//
//  ZegoVideoCallback.h
//  ZegoVideoSDK
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TextMsg.h"

#define CREATE_TIME @"created_time"    //uint32
#define SHOW_TYPE @"show_type"         //uint32
#define PLAYER_COUNT @"player_count"   //在线观看人数 uint32
#define ZEGO_ID @"zego_id"             //uint32
#define ZEGO_TOKEN @"zego_token"       //uint32
#define SHOW_PUBLISH_LIST @"publisherlist"  //NSArray
#define PUBLISH_EXTRA_DATAS @"publish_extra_datas"  //NSDictionary


#define SHOW_TITLE @"show_title"                //NSString
#define PUBLISHER_ID @"publisher_id"            //NSString
#define PUBLISHER_NAME @"publisher_name"        //NSString
#define SHOW_PREVIEW_PIC_URL @"show_preview_pic_url" //NSString

//replay
#define BEGIN_TIME @"begin_time"  //uint32
#define END_TIME @"end_time"      //uint32
#define REPLAY_URLS @"replay_urls" //NSArray

@protocol ZegoShowListDelegate <NSObject>

typedef unsigned int	uint32;
/*
typedef enum {
    GetShowListResult_Succ = 0,
    GetShowListResult_No_ShowList = 1,
    GetShowListResult_err = 2
} GetShowListResult;
 */

/// \brief 拉取直播列表的通知
/// \note 调用getShowList后，会收到onGetShowListResult
/// \param result 0表示获取成功，其他表示失败
/// \param totalCount 总的直播数
/// \param beginIndex 拉直播列表的起始位置
/// \param showList 直播列表
/// list
///   |  -- NSDictionary(CREATE_TIME, SHOW_TYPE, PLAYER_COUNT, ZEGO_ID, ZEGO_TOKEN, SHOW_PUBLISH_LIST, PUBLISH_EXTRA_DATAS)
///               | -- NSArray(SHOW_PUBLISH_LIST)
///                     |  -- NSDictionary(SHOW_TITLE, PUBLISHER_ID, PUBLISHER_NAME, SHOW_PREVIEW_PIC_URL)
///               | -- NSDictionary(额外数据，setPublishExtraData时，设定的key)
///   |  -- NSDictionary
///               | -- NSArray
///                     |  -- NSDictionary
///               | -- NSDictionary
///   |  -- ...
- (void) onGetShowListResult:(uint32)result totalCount:(uint32)totalCount beginIndex:(uint32)beginIndex showList:(NSArray*)list;

/// \brief 拉取回播列表的通知
/// \note 调用getReplayList后，会收到onGetReplayListResult
/// \param result 0表示获取成功，其他表示失败
/// \param totalCount 总的回播数
/// \param beginIndex 拉回播列表的起始位置
/// \param replayList 回播列表
/// list
///   |  -- NSDictionary(PLAYER_COUNT, SHOW_PUBLISH_LIST, PUBLISH_EXTRA_DATAS)
///               | -- NSArray(SHOW_PUBLISH_LIST)
///                     |  -- NSDictionary(SHOW_TITLE, PUBLISHER_ID, PUBLISHER_NAME, SHOW_PREVIEW_PIC_URL, BEGIN_TIME, END_TIME, REPLAY_URLS)
///                             | -- NSArray(REPLAY_URLS)
///                                     | -- NSString(回放的Url列表，目前一个回放只会有一个URL)
///               | -- NSDictionary(额外数据，setPublishExtraData时，设定的key)
///   |  -- NSDictionary
///               | -- NSArray
///                     |  -- NSDictionary
///               | -- NSDictionary(额外数据，setPublishExtraData时，设定的key)
///   |  -- ...
- (void) onGetReplayListResult:(uint32)result totalCount:(uint32)totalCount beginIndex:(uint32)beginIndex replayList:(NSArray*)list;

@end
