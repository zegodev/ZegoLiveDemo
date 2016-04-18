//
//  ZegoShareApi.h
//  zegoavkit
//
//  Copyright © 2016 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>



extern const NSString *kZegoShareStreamKey;
extern const NSString *kZegoShareUrlKey;
extern const NSString *kZegoRtmpPlayUrlKey;
extern const NSString *kZegoHlsPlayUrlKey;


@protocol ZegoShareApiDelegate <NSObject>

/// \brief 获取直播流分享 url 结果
/// \param errorCode 错误码，0 成功，非 0 失败
/// \param info 获取结果：包含 kZegoShareStreamKey/kZegoShareUrlKey
- (void)onGetStreamShareUrl:(int)errorCode info:(NSDictionary *)info;

/// \brief 获取直播流分享 url 结果
/// \param errorCode 错误码，0 成功，非 0 失败
/// \param info 获取结果：包含 kZegoShareStreamKey/kZegoRtmpPlayUrlKey/kZegoHlsPlayUrlKey
- (void)onGetStreamPlayUrl:(int)errorCode info:(NSDictionary *)info;

@end


@interface ZegoShareApi : NSObject

- (void)initApi;
- (void)uninitApi;

/// \brief 获取直播流分享 url
/// \param baseUrl 基地址，如 http://api.zego.im
/// \param stream 流标识，如 s-1234-5678-9
/// \param title 流名字
/// \return true 请求成功，等待回调 onGetStreamShareUrl:info: 返回结果, false 请求失败
- (BOOL)getShareUrlByBase:(NSString *)baseUrl stream:(NSString *)stream title:(NSString *)title;

/// \brief 获取直播流播放 url
/// \param stream 流标识，如 s-1234-5678-9
/// \return ture 请求成功，等待回调 onGetStreamPlayUrl:info: 返回结果, false 请求失败
- (BOOL)getStreamPlayUrl:(NSString *)stream;

- (void)setDelegate:(id<ZegoShareApiDelegate>)delegate callbackQueue:(dispatch_queue_t)q;

@end
