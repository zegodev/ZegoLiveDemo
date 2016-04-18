//
//  LiveRoomViewController.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>
#include "ZegoAVKitManager.h"
#import "ZegoAVKit/ZegoMoviePlayer.h"
#import <ZegoAVKit/ZegoShareApi.h>


#define  LIVEROOM_TYPE_PUBLISH @"1"
#define  LIVEROOM_TYPE_PLAY @"2"
#define  LIVEROOM_TYPE_PLAYBACK @"3"
#define  COVER_FILE_KEY @"Cover_Pic"


@interface LiveRoomViewController : UIViewController<ZegoChatDelegate, ZegoVideoDelegate, UIApplicationDelegate, ZegoMoviePlayerDelegate, UITableViewDataSource, UITableViewDelegate, ZegoShareApiDelegate>

    @property(nonatomic,strong) NSString * roomType;
    @property(nonatomic,strong) NSString * roomTitle;
    @property(nonatomic,strong) NSString * roomNumber;
    @property(nonatomic,strong) NSString * roomToken;
    @property(nonatomic,strong) NSString * createTime;
    @property(nonatomic,strong) NSString * endTime;
    @property(nonatomic,strong) NSString * userName;
    @property(nonatomic,strong) NSString * userID;
    @property(nonatomic,strong) NSString * userPic;
    @property(nonatomic,strong) NSString * publisherName;
    @property(nonatomic,strong) NSString * publisherID;
    @property(nonatomic,strong) NSString * publisherPic;
    @property(nonatomic,strong) NSString * replayPath;
    @property(nonatomic,strong) NSString * coverPath;



    @property (weak, nonatomic) IBOutlet UIButton *publishControlBtn;
    @property (weak, nonatomic) IBOutlet UIButton *switchCameraBtn;
    @property (weak, nonatomic) IBOutlet UITextField *inputMsgEdit;

    @property (weak, nonatomic) IBOutlet UIButton *playerCountBtn;
    @property (weak, nonatomic) IBOutlet UIButton *chatDisplayCtrlBtn;
    @property (weak, nonatomic) IBOutlet UITextView *messageBoard;
    @property (weak, nonatomic) IBOutlet UIImageView *publisherPicView;
    @property (weak, nonatomic) IBOutlet UIButton *likeBtn;

    @property (weak, nonatomic) IBOutlet UILabel *roomStatus;
    @property (weak, nonatomic) IBOutlet UILabel *liveDuration;
    @property (weak, nonatomic) IBOutlet UIScrollView *playerListView;
    @property (weak, nonatomic) IBOutlet UILabel *likeCountLabel;
    @property (weak, nonatomic) IBOutlet UIButton *goPublishBtn;
    @property (weak, nonatomic) IBOutlet UIButton *switchViewBtn;

    @property (weak, nonatomic) IBOutlet UIView *secondStreamView;
    @property (weak, nonatomic) IBOutlet UIView *firstStreamView;

    @property (weak, nonatomic) IBOutlet UIView *replayCtrlView;
    @property (weak, nonatomic) IBOutlet UIButton *replayCtrlBtn;

    @property (weak, nonatomic) IBOutlet UISlider *replayProgress;
    @property (weak, nonatomic) IBOutlet UILabel *replayDurationLabel;

    @property (weak, nonatomic) IBOutlet UITableView *candidateTableView;

    - (IBAction)switchCameraBtnClicked:(UIButton *)sender;
    - (IBAction)publishControlBtnClicked:(UIButton *)sender;
    - (IBAction)chatDisplayCtrlBtnClicked:(UIButton *)sender;
    - (IBAction)likeBtnClicked:(UIButton *)sender;
    - (IBAction)goPublishBtnClicked:(UIButton *)sender;
    - (IBAction)switchViewBtnClicked:(UIButton *)sender;

    - (IBAction)replayCtrlClicked:(UIButton *)sender;
    - (IBAction)replayProgressChanged:(UISlider *)sender;

    - (IBAction)viewTouchDown:(UIControl *)sender;

    @property bool useFrontCamera;


@end
