
#import <Foundation/Foundation.h>

#if TARGET_OS_IPHONE
#import <ZegoAVKit/ZegoUser.h>
#elif TARGET_OS_MAC
#import <ZegoAVKitosx/ZegoUser.h>
#endif


@interface TextMsg : NSObject

@property (nonatomic,retain) ZegoUser* user;

@property (nonatomic) time_t sendTime;

@property (nonatomic,retain) NSString* msg;


- (instancetype)init;

@end
