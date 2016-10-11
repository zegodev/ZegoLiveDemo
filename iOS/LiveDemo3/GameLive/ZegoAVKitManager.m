//
//  ZegoAVKitManager.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#include "ZegoAVKitManager.h"
#import <ZegoAVKit2/ZegoVideoCapture.h>
#import <ZegoLiveRoom/BizLiveRoom.h>

static ZegoAVKitManager *avkitManager;

@interface ZegoAVKitManager () <ZegoLiveApiDelegate, BizRoomStreamDelegate>

@property (nonatomic, strong, readonly) BizLiveRoom *zegoLiveRoom;
@property (nonatomic, strong, readonly) ZegoLiveApi *zegoLiveApi;

@property (nonatomic, strong) NSString *liveTitle;
@property (nonatomic, assign) BOOL requiredHardwareAccelerate;
@property (nonatomic, assign) BOOL testEnvironment;

@property (nonatomic, strong) NSString *userID;
@property (nonatomic, strong) NSString *userName;
@property (nonatomic, strong) NSString *liveChannel;
@property (nonatomic, strong) NSString *streamID;

@property (nonatomic, assign) CGSize videoSize;

@end

@implementation ZegoAVKitManager

+ (instancetype)sharedInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        avkitManager = [[ZegoAVKitManager alloc] init];
    });
    
    return avkitManager;
}

- (instancetype)init
{
    self = [super init];
    if (self)
    {
        _requiredHardwareAccelerate = YES;
        _testEnvironment = NO;
        
        [self initZegoLiveApi];
        [self initZegoLiveRoom];
        
        _zegoLiveApi.delegate = self;
        _zegoLiveRoom.streamDelegate = self;
        
        NSUserDefaults *sharedDefaults = [[NSUserDefaults alloc] initWithSuiteName:@"group.liveDemo3"];
        self.userID = [sharedDefaults stringForKey:@"userid"];
        self.userName = [sharedDefaults stringForKey:@"username"];
        if (self.userID.length == 0)
        {
            srand((unsigned)time(0));
            _userID = [NSString stringWithFormat:@"%u", (unsigned)rand()];
            [sharedDefaults setObject:_userID forKey:@"userid"];
            
#if TARGET_OS_SIMULATOR
            _userName = [NSString stringWithFormat:@"simulator-%u", (unsigned)rand()];
#else
            _userName = [NSString stringWithFormat:@"iphone-%u", (unsigned)rand()];
#endif
            [sharedDefaults setObject:_userName forKey:@"username"];
        }
        
        NSLog(@"userID %@", self.userID);
        NSLog(@"userName %@", self.userName);
    }
    
    return self;
}

- (void)initZegoLiveApi
{
    if (self.zegoLiveApi != nil)
        return;
    
    [ZegoLiveApi setLogLevel:4];
    [ZegoLiveApi setUseTestEnv:self.testEnvironment];
    
    [ZegoLiveApi prepareReplayLiveCapture];
    
    NSData *appSign = [self getZegoAppSign];
    _zegoLiveApi = [[ZegoLiveApi alloc] initWithAppID:1 appSignature:appSign];
    
    [self.zegoLiveApi requireHardwareAccelerated:self.requiredHardwareAccelerate];
}

- (void)initZegoLiveRoom
{
    if (self.zegoLiveRoom != nil)
        return;
    
    [BizLiveRoom setLogLevel:4];
    
    NSData *appSign = [self getZegoAppSign];
    _zegoLiveRoom = [[BizLiveRoom alloc] initWithBizID:1 bizSignature:appSign];
}

- (NSData *)getZegoAppSign
{
    Byte signkey[] = {0x91,0x93,0xcc,0x66,0x2a,0x1c,0xe,0xc1,0x35,0xec,0x71,0xfb,0x7,0x19,0x4b,0x38,0x15,0xf1,0x43,0xf5,0x7c,0xd2,0xb5,0x9a,0xe3,0xdd,0xdb,0xe0,0xf1,0x74,0x36,0xd};
    
    return [NSData dataWithBytes:signkey length:32];
}

- (void)handleVideoInputSampleBuffer:(CMSampleBufferRef)sampleBuffer
{
    [self.zegoLiveApi handleVideoInputSampleBuffer:sampleBuffer];
}

- (void)handleAudioInputSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType
{
    [self.zegoLiveApi handleAudioInputSampleBuffer:sampleBuffer withType:sampleBufferType];
}

- (void)startLiveWithTitle:(NSString *)liveTitle videoSize:(CGSize)videoSize
{
    if (liveTitle.length == 0)
        self.liveTitle = [NSString stringWithFormat:@"Hello-%@", self.userName];
    else
        self.liveTitle = liveTitle;
    
    self.videoSize = videoSize;
    NSLog(@"videoSize %@", NSStringFromCGSize(videoSize));
    
    [self loginChatRoom];
}

- (void)stopLive
{
    [self.zegoLiveApi stopPublishing];
    [self reportStreamAction:NO streamID:self.streamID];
    
    [self.zegoLiveApi logoutChannel];
    [self.zegoLiveRoom leaveLiveRoom:YES];
}

- (void)loginChatRoom
{
    NSString *userName = [NSString stringWithFormat:@"#d-%@", self.userName];
    [self.zegoLiveRoom loginLiveRoom:self.userID userName:userName bizToken:0 bizID:0 isPublicRoom:YES];
    
    NSLog(@"loginChatRoom %@", userName);
}

- (void)createStream:(NSString *)preferredStreamID
{
    [self.zegoLiveRoom cteateStreamInRoom:self.liveTitle preferredStreamID:preferredStreamID isPublicRoom:YES];
    
    NSLog(@"%s, %@", __func__, preferredStreamID);
}

- (NSString *)getChannelID:(unsigned int)bizToken bizID:(unsigned int)bizID
{
    return [NSString stringWithFormat:@"0x%x-0x%x", bizID, bizToken];
}

- (void)loginChannel
{
    ZegoUser *user = [[ZegoUser alloc] init];
    user.userID = self.userID;
    user.userName = self.userName;
    
    [self.zegoLiveApi loginChannel:self.liveChannel user:user];
}

- (void)reportStreamAction:(BOOL)success streamID:(NSString *)streamID
{
    if (success)
        [self.zegoLiveRoom reportStreamAction:1 streamID:streamID userID:self.userID isPublicRoom:YES];
    else
        [self.zegoLiveRoom reportStreamAction:2 streamID:streamID userID:self.userID isPublicRoom:YES];
}

#pragma mark ZegoLiveApiDelegate
- (void)onLoginChannel:(NSString *)channel error:(uint32)err
{
    NSLog(@"%s, err: %u", __func__, err);
    if (err != 0)
    {
        return;
    }
    else
    {
        ZegoAVConfig *config = [ZegoAVConfig new];
        config.videoEncodeResolution = self.videoSize;
        config.videoCaptureResolution = self.videoSize;
        config.fps = 30;
        config.bitrate = 800000;
        [self.zegoLiveApi setAVConfig:config];
        
        [self.zegoLiveApi startPublishingWithTitle:self.liveTitle streamID:self.streamID];
    }
}

- (void)onPublishSucc:(NSString *)streamID channel:(NSString *)channel streamInfo:(NSDictionary *)info
{
    NSLog(@"%s, info %@", __func__, info);
    
    [self reportStreamAction:YES streamID:streamID];
}

- (void)onPublishStop:(uint32)err stream:(NSString *)streamID channel:(NSString *)channel
{
    NSLog(@"%s, streamID %@", __func__, streamID);
    
    [self reportStreamAction:NO streamID:streamID];
}

#pragma mark BizRoomStreamDelegate
- (void)onLoginRoom:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken isPublicRoom:(bool)isPublicRoom
{
    NSLog(@"%s, error: %d", __func__, err);
    
    if (err == 0)
    {
        self.liveChannel = [self getChannelID:bizToken bizID:bizID];
        [self createStream:nil];
    }
    else
    {
        NSLog(@"login chatRoom error %d", err);
    }
}

- (void)onStreamCreate:(NSString *)streamID url:(NSString *)url isPublicRoom:(bool)isPublicRoom
{
    if (streamID.length != 0)
    {
        self.streamID = streamID;
        [self loginChannel];
    }
    else
    {
        NSLog(@"stream create error");
    }
}
@end
