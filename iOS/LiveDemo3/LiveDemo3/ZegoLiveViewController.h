//
//  ZegoLiveViewController.h
//  LiveDemo3
//
//  Created by Strong on 16/6/28.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ZegoAVKitManager.h"
#import "ZegoAnchorOptionViewController.h"
#import <AVFoundation/AVAudioSession.h>

#define MAX_STREAM_COUNT    3

@interface ZegoLiveViewController : UIViewController <ZegoAnchorOptionDelegate>

//YES 使用前置摄像头
@property (nonatomic, assign) BOOL useFrontCamera;
//YES 开启麦克风
@property (nonatomic, assign) BOOL enableMicrophone;
//设置美颜效果
@property (nonatomic, assign) ZegoBeautifyFeature beautifyFeature;
//设置滤镜
@property (nonatomic, assign) ZegoFilter filter;
//设置是否支持预览
@property (nonatomic, assign) BOOL enablePreview;
//设置视频view格式（等比缩放，等比缩放填充，填充整个视图等)
@property (nonatomic, assign) ZegoVideoViewMode viewMode;
//YES 开启手电筒
@property (nonatomic, assign) BOOL enableTorch;
//YES 启用摄像头 
@property (nonatomic, assign) BOOL enableCamera;
//YES 扬声器打开
@property (nonatomic, assign) BOOL enableSpeaker;
//YES 混音
@property (nonatomic, assign) BOOL enableAux;

//日志记录
@property (nonatomic, strong) NSMutableArray *logArray;

- (void)setAnchorConfig:(UIView *)publishView;

- (BOOL)isDeviceiOS7;

- (BOOL)setContainerConstraints:(UIView *)view containerView:(UIView *)containerView viewCount:(NSUInteger)viewCount;
- (void)updateContainerConstraintsForTap:(UIView *)tapView containerView:(UIView *)containerView;
- (void)updateContainerConstraintsForRemove:(UIView *)removeView containerView:(UIView *)containerView;

- (void)reportStreamAction:(BOOL)success streamID:(NSString *)streamID;

- (void)showPublishOption;

//收到请求上台消息，弹框提示
- (void)requestPublishAlert:(ZegoUser *)requestUser magicNumber:(NSString *)magicNumber;
//被主播拒绝后的提示
- (void)requestPublishResultAlert:(NSString *)fromUserName;

- (void)onReceivePublishRequest:(NSDictionary *)receiveInfo;

- (void)setIdelTimerDisable:(BOOL)disable;
- (void)dismissAlertView:(NSString *)magicNumber;

//电话监听处理函数
- (void)audioSessionWasInterrupted:(NSNotification *)notification;

- (void)addLogString:(NSString *)logString;

- (void)updateQuality:(int)quality view:(UIView *)playerView;

//混流时的回调实现
- (void)auxCallback:(void *)pData dataLen:(int *)pDataLen sampleRate:(int *)pSampleRate channelCount:(int *)pChannelCount;

//获取第一个view
- (UIView *)getFirstViewInContainer:(UIView *)containerView;

@end
