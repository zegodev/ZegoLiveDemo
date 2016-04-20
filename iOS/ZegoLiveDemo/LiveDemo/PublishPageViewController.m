//
//  PublishPageViewController.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import "PublishPageViewController.h"
#import <ZegoAVKit/ZegoAVApi.h>
#import <ZegoAVKit/ZegoAVConfig.h>
#import "ZegoAVKitManager.h"
#import "LiveRoomViewController.h"


@interface PublishPageViewController ()

@property (weak, nonatomic) IBOutlet UIView *beautyBox;
@property (weak, nonatomic) IBOutlet UIView *filterBox;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *beautifyBoxHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *beautifyBoxWidth;
@property (weak, nonatomic) IBOutlet UIButton *btnBeautify;
@property (weak, nonatomic) IBOutlet UIPickerView *beautifyPicker;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *filterBoxHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *filterBoxWidth;
@property (weak, nonatomic) IBOutlet UIPickerView *filterPicker;
@property (weak, nonatomic) IBOutlet UIButton *btnFilter;

@property (readonly) NSArray* beautifyFeatureList;

@end

@implementation PublishPageViewController
{
    BOOL useFrontCamera;
    BOOL enableTorch;
    
    NSString *coverImagePath;
    NSArray *_filterList;
}

- (void)viewDidLoad {
    [super viewDidLoad];

    _coverImageView.userInteractionEnabled = YES;
    UIGestureRecognizer *singleTap =  [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(coverImageViewTap:)];
    [_coverImageView addGestureRecognizer:singleTap];
    
    [_coverImageView setContentMode:UIViewContentModeScaleAspectFit];


    //默认使用后摄像头
    useFrontCamera = NO;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillAppear:(BOOL)animated{
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    NSString *strAccountName = [ud stringForKey:@"accountName"];
    
    _publishDescription.text = [[NSString alloc] initWithFormat:@"%@直播秀", strAccountName];
    
    //NSString *strUserPic = [ud stringForKey:@"accountPic"];
    //[_coverImageView setImage:[UIImage imageNamed:strUserPic]];
    
    
    [self registerForKeyboardNotifications];
    
    [self swichPreviewChanged:_previewSwitch];
    
    _filterList = @[
                    @"无滤镜",
                    @"简洁",
                    @"黑白",
                    @"老化",
                    @"哥特",
                    @"锐色",
                    @"淡雅",
                    @"酒红",
                    @"青柠",
                    @"浪漫",
                    @"光晕",
                    @"蓝调",
                    @"梦幻",
                    @"夜色"
                    ];
    
    _beautifyFeatureList = @[@"无美颜", @"磨皮", @"全屏美白", @"磨皮＋全屏美白", @"磨皮+皮肤美白"];
    
    self.filterBox.hidden = YES;
    self.beautyBox.hidden = YES;
}

- (void)viewWillDisappear:(BOOL)animated{
    [self stopPreview];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (IBAction)View_TouchDown:(id)sender {
    // 发送resignFirstResponder.
    [[UIApplication sharedApplication] sendAction:@selector(resignFirstResponder) to:nil from:nil forEvent:nil];
}
- (IBAction)publishDescription_DidEndOnExit:(id)sender {
    // 隐藏键盘.
    [sender resignFirstResponder];
    // 触发登陆按钮的点击事件.
    [self.btnBeginPublish sendActionsForControlEvents:UIControlEventTouchUpInside];
}


#pragma mark - Navigation

#define  LIVEROOM_TYPE_PUBLISH @"1"
#define  LIVEROOM_TYPE_PLAY @"2"
#define  LIVEROOM_TYPE_REPLAY @"3"


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(nullable id)sender{
    
    if([segue.identifier isEqualToString:@"PublishInLiveRoom"] == YES){
        id liveRoomViewController = segue.destinationViewController;
        
        if ([liveRoomViewController isKindOfClass:[liveRoomViewController class]]) {
            LiveRoomViewController *lc = (LiveRoomViewController *)liveRoomViewController;
            
            lc.useFrontCamera = useFrontCamera;
            lc.roomType = LIVEROOM_TYPE_PUBLISH;
            lc.roomTitle = self.publishDescription.text;
            
            NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
            NSString *strAccountName = [ud stringForKey:@"accountName"];
            NSString *strAccountID = [[NSString alloc] initWithFormat:@"%u", (UInt32)[ud doubleForKey:@"accountID"]];
            NSString *strAccountPic = [ud stringForKey:@"accountPic"];
            lc.userName = strAccountName;
            lc.userID = strAccountID;
            lc.userPic = strAccountPic;
            
            lc.coverPath = coverImagePath;
        }
    }
}

- (IBAction)switchCameraClicked:(UIButton *)sender {
    useFrontCamera = !useFrontCamera;
    
    [getZegoAV_ShareInstance() setFrontCam:useFrontCamera];
}

- (IBAction)enableTorchClicked:(UIButton *)sender {
    enableTorch = !enableTorch;

    [getZegoAV_ShareInstance() enableTorch:enableTorch];

}

- (IBAction)swichPreviewChanged:(UISwitch *)sender {
    if (sender.on) {
        [self startPreview];
        self.filterBox.hidden = NO;
        self.beautyBox.hidden = NO;
        self.coverImageView.hidden = YES;
    }
    else{
        [self stopPreview];
        self.filterBox.hidden = YES;
        self.beautyBox.hidden = YES;
        self.coverImageView.hidden = NO;
    }
}

#pragma mark -- UIGestureRecognizerDelegate
- (void)coverImageViewTap:(UIGestureRecognizer *)sender{
    
    UIActionSheet *sheet;
    
    // 判断是否支持相机
    if([UIImagePickerController
        isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]){
        
        sheet = [[UIActionSheet alloc] initWithTitle:@"选择图像" delegate:self cancelButtonTitle:nil destructiveButtonTitle:@"取消" otherButtonTitles:@"使用动态截屏", @"从相册选择", @"拍照", nil];
    }
    else{
        sheet = [[UIActionSheet alloc] initWithTitle:@"选择图像" delegate:self cancelButtonTitle:nil destructiveButtonTitle:@"取消" otherButtonTitles:@"使用动态截屏", @"从相册选择", nil];
    }
    sheet.tag = 255;
    
    [sheet showInView:self.view];
}

#pragma mark - action sheet delegte
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (actionSheet.tag == 255) {
        NSUInteger sourceType = UIImagePickerControllerSourceTypePhotoLibrary;

        switch (buttonIndex) {
            case 0:
                return;
                
            case 1: //使用动态截屏
                [_coverImageView setImage:[UIImage imageNamed:@"LiveCapture.png"]];
                coverImagePath = @"";
                return;
            
            case 2: //相册
                sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
                break;
            
            case 3: //相机
                sourceType = UIImagePickerControllerSourceTypeCamera;
                break;
        }
        
        // 跳转到相机或相册页面
        UIImagePickerController *imagePickerController = [[UIImagePickerController alloc] init];
        imagePickerController.delegate = self;
        imagePickerController.allowsEditing = YES;
        imagePickerController.videoQuality = UIImagePickerControllerQualityType640x480;
        imagePickerController.sourceType = sourceType;
        [self presentViewController:imagePickerController animated:YES completion:^{}];
    }
}

#pragma mark - image picker delegte
-(void)imagePickerController:(UIImagePickerController*)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    [picker dismissViewControllerAnimated:YES completion:^{
        UIImage * image=[info objectForKey:UIImagePickerControllerEditedImage];
        
        CGSize size = CGSizeMake(240, 320);
        image = [self scaleImageToSize:image scaleToSize:size];
        
        NSData *data;
        if (UIImagePNGRepresentation(image) == nil){
            data = UIImageJPEGRepresentation(image, 1.0);
        }
        else{
            data = UIImagePNGRepresentation(image);
        }
        
        //将图片保存在沙盒的documents文件夹中
        NSString * documentsPath = [NSHomeDirectory() stringByAppendingPathComponent:@"Documents"];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        
        //把data对象拷贝至沙盒中 并保存为coverImage.png
        [fileManager createDirectoryAtPath:documentsPath withIntermediateDirectories:YES attributes:nil error:nil];
        [fileManager createFileAtPath:[documentsPath stringByAppendingString:@"/coverImage.png"] contents:data attributes:nil];
        
        coverImagePath = [[NSString alloc]initWithFormat:@"%@%@",documentsPath,  @"/coverImage.png"];
        
        
        //UIImage *compressedImage = [UIImage imageWithData:data];
        
        [_coverImageView setImage:image];//]compressedImage];
        
    }];
    
}

-(void)imagePickerControllerDIdCancel:(UIImagePickerController*)picker
{
    [picker dismissViewControllerAnimated:YES completion:^{}];
}


- (void)registerForKeyboardNotifications
{
    //使用NSNotificationCenter
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWasShown:) name:UIKeyboardDidShowNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardDismiss:) name:UIKeyboardDidHideNotification object:nil];
    
}

//实现当键盘出现的时候计算键盘的高度大小。用于输入框显示位置
- (void)keyboardWasShown:(NSNotification*)aNotification
{
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue].size;
    CGRect edFrame = self.view.frame;
    edFrame.size.height = [UIScreen mainScreen].applicationFrame.size.height - kbSize.height + 80;
    self.view.frame = edFrame;
}

- (void)handleKeyboardDismiss:(NSNotification *)notification {
    CGRect edFrame = self.view.frame;
    edFrame.size.height = [UIScreen mainScreen].applicationFrame.size.height;
    self.view.frame = edFrame;
}

- (void)startPreview{
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];

    NSInteger avConfigPreset = [ud integerForKey:@"avConfigPreset"];
    
    ZegoAVConfig *zegoAVConfig;
    if (avConfigPreset < 0) {
        //用户自定义过各种参数
        NSInteger nResolutionEnum = [ud integerForKey:@"avConfigResolution"];
        NSInteger nFPS = [ud integerForKey:@"avConfigFPS"];
        NSInteger nBitrate = [ud integerForKey:@"avConfigBitrate"];
        
        zegoAVConfig = [ZegoAVConfig new];
        
        [zegoAVConfig setVideoFPS:(int)nFPS];
        [zegoAVConfig setVideoBitrate:(int)nBitrate];
        [zegoAVConfig setVideoResolution:(int)nResolutionEnum];
    }
    else{
        zegoAVConfig = [ZegoAVConfig defaultZegoAVConfig:(ZegoAVConfigPreset)avConfigPreset];
    }
    [getZegoAV_ShareInstance() setAVConfig:zegoAVConfig];
    
    [getZegoAV_ShareInstance() setLocalView:self.view];
    [getZegoAV_ShareInstance() setFrontCam:useFrontCamera];
    
    //[getZegoAV_ShareInstance() setLocalViewMode:ZegoVideoViewModeScaleAspectFill];   //设置预览画面保留比例留黑边，不做裁剪
    
    [getZegoAV_ShareInstance() startPreview];
}

- (void)stopPreview{
    [getZegoAV_ShareInstance() setLocalView:nil];

    [getZegoAV_ShareInstance() stopPreview];
}


- (UIImage*) scaleImageToSize:(UIImage *)image scaleToSize:(CGSize)size{
    
    CGImageRef imageRef = nil;

    float fSrcR = (float)image.size.height / image.size.width;
    float fDstR = (float)size.height / size.width;
    
    CGSize newSize = size;
    CGRect dstRect = CGRectMake(0, 0, size.width, size.height);
    
    if(fSrcR > fDstR)
    {
        newSize.height = size.width * fSrcR;

        dstRect.origin.y = (newSize.height - size.height) / 2;
    }
    else if(fSrcR < fDstR)
    {
        newSize.width = size.height / fSrcR;

        dstRect.origin.x = (newSize.width - size.width) / 2;
    }

    UIGraphicsBeginImageContextWithOptions(newSize, YES, 1.0);
    [image drawInRect:CGRectMake(0,0,newSize.width,newSize.height)];
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    imageRef = CGImageCreateWithImageInRect([scaledImage CGImage], dstRect);

    return [UIImage imageWithCGImage:imageRef];
}


#pragma mark -- UIPickerViewDelegate, UIPickerViewDataSource
// pickerView 列数
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

// pickerView 每列个数
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    if (pickerView == self.beautifyPicker) {
        return self.beautifyFeatureList.count;
    } else {
        return _filterList.count;
    }
}


// 返回选中的行
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    if (pickerView == self.beautifyPicker) {
        int feature = ZEGO_BEAUTIFY_NONE;
        switch (row) {
            case 1:
                feature = ZEGO_BEAUTIFY_POLISH;
                break;
            case 2:
                feature = ZEGO_BEAUTIFY_WHITEN;
                break;
            case 3:
                feature = ZEGO_BEAUTIFY_POLISH | ZEGO_BEAUTIFY_WHITEN;
                break;
            case 4:
                feature = ZEGO_BEAUTIFY_POLISH | ZEGO_BEAUTIFY_SKINWHITEN;
                break;
            default:
                break;
        }
        
        [getZegoAV_ShareInstance() enableBeautifying:feature];
        
        [self.btnBeautify setTitle:self.beautifyFeatureList[row] forState:UIControlStateNormal];
        
    } else {
        [getZegoAV_ShareInstance() setFilter:row];
        [self.btnFilter setTitle:_filterList[row] forState:UIControlStateNormal];
    }
}

//返回当前行的内容,此处是将数组中数值添加到滚动的那个显示栏上
-(NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    NSArray *dataList = nil;
    if (pickerView == self.beautifyPicker) {
        dataList = self.beautifyFeatureList;
    } else {
        dataList = _filterList;
    }
    
    if (row >= dataList.count) {
        return @"Error";
    }
    
    return [dataList objectAtIndex:row];
}


- (IBAction)enableBeautify:(id)sender {
    
    if (self.beautifyBoxWidth.constant == 150) {
        self.beautifyBoxHeight.constant = 46;
        self.beautifyBoxWidth.constant = 120;
        self.beautifyPicker.hidden = YES;
    } else {
        self.beautifyBoxHeight.constant = 128;
        self.beautifyBoxWidth.constant = 150;
        self.beautifyPicker.hidden = NO;
    }
    
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}

- (IBAction)filterClicked:(id)sender {
    if (self.filterBoxWidth.constant == 130) {
        self.filterBoxHeight.constant = 46;
        self.filterBoxWidth.constant = 60;
        self.filterPicker.hidden = YES;
    } else {
        self.filterBoxHeight.constant = 128;
        self.filterBoxWidth.constant = 130;
        self.filterPicker.hidden = NO;
    }
    
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}


@end
