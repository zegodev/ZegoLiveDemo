//
//  ProfileViewController.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import "ProfileViewController.h"
#import "ZegoAVKit/ZegoAVConfig.h"
#import "ZegoAVKitManager.h"

@interface ProfileViewController ()
@end

@implementation ProfileViewController
{
    NSArray *listPresetLiveQuality;
    int nHitCount;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    listPresetLiveQuality = [[NSArray alloc]initWithObjects:@"超低质量",@"低质量",@"标准质量",@"高质量", @"超高质量", @"自定义", nil];
    
    self.liveQuality.showsSelectionIndicator=YES;
    self.liveQuality.dataSource = self;
    self.liveQuality.delegate = self;
    
    [self loadUserDefaults];
    
    
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self registerNotifications];
}

- (void)registerNotifications
{
    //使用NSNotificationCenter
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWasShown:) name:UIKeyboardDidShowNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardDismiss:) name:UIKeyboardDidHideNotification object:nil];
}

- (void)viewWillDisappear:(BOOL)animated{
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    
    [ud synchronize];
    //释放
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (NSData*)ConvertStringToSign:(NSString*) strSign{
    if(strSign == nil || strSign.length == 0)
    return nil;
    strSign = [strSign lowercaseString];
    strSign = [strSign stringByReplacingOccurrencesOfString:@" " withString:@""];
    strSign = [strSign stringByReplacingOccurrencesOfString:@"0x" withString:@""];
    NSArray* szStr = [strSign componentsSeparatedByString:@","];
    int nLen = (int)[szStr count];
    Byte szSign[32];
    for(int i = 0; i < nLen; i++)
    {
        NSString *strTmp = [szStr objectAtIndex:i];
        if(strTmp.length == 1)
        szSign[i] = [self toByte:strTmp];
        else
        {
            szSign[i] = [self toByte:[strTmp substringWithRange:NSMakeRange(0, 1)]]<< 4 | [self toByte:[strTmp substringWithRange:NSMakeRange(1, 1)]];
        }
        NSLog(@"%x,", szSign[i]);
    }
    
    NSData *sign = [[NSData alloc]initWithBytes:szSign length:32];
    return sign;
}

- (Byte) toByte:(NSString*)c {
    NSString *str = @"0123456789abcdef";
    Byte b = [str rangeOfString:c].location;
    return b;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

//实现当键盘出现的时候计算键盘的高度大小。用于输入框显示位置
- (void)keyboardWasShown:(NSNotification*)aNotification
{
    if (self.accountName.isFirstResponder) {
        return;
    }
    
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue].size;
    CGRect edFrame = self.view.frame;
    //edFrame.size.height = [UIScreen mainScreen].applicationFrame.size.height - kbSize.height;
    edFrame.origin.y = 0 - kbSize.height;
    self.view.frame = edFrame;
}

- (void)handleKeyboardDismiss:(NSNotification *)notification {
    CGRect edFrame = self.view.frame;
    edFrame.origin.y = 0;
    self.view.frame = edFrame;
    
    NSString *strIP = self.tfIP.text;
    NSString *strPort = self.tfPort.text;
    NSString *strUrl = self.tfUrl.text;
    if ((self.tfIP.text.length != 0 && self.tfPort.text.length != 0) || self.tfUrl.text.length != 0)
    {
        [getZegoAV_ShareInstance() setTestServer:strIP port:[strPort intValue] url:strUrl];
        setTestServer(strIP, [strPort intValue], strUrl);
    }
    if (self.tfAppID.text.length != 0 && self.tfSign.text.length != 0) {
        NSData *sign = [self ConvertStringToSign:self.tfSign.text];
        setCustomAppIDAndSign([self.tfAppID.text intValue], sign);
        releaseZegoAV_ShareInstance();
    }
}

#pragma mark -- UIPickerViewDelegate, UIPickerViewDataSource
// pickerView 列数
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

// pickerView 每列个数
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return [listPresetLiveQuality count];
}


// 返回选中的行
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    NSInteger avConfigPreset = row;
    if (row < listPresetLiveQuality.count-1) {
        [self updateLiveQualityDetails:(int)row];
    }
    else{   //自定义
        avConfigPreset = -1;
    }
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setInteger:avConfigPreset forKey:@"avConfigPreset"];
}

//返回当前行的内容,此处是将数组中数值添加到滚动的那个显示栏上
-(NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return [listPresetLiveQuality objectAtIndex:row];
    
}


- (void)loadUserDefaults{
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    if ([ud objectForKey:@"accountID"] == nil){
        [self generateAcount];
        [self presetLiveQuality];
        
        self.lablePrompt.hidden = false;
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 3 * NSEC_PER_SEC);
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
            self.lablePrompt.hidden = true;
        });
    }
    
    self.accountID.text = [[NSString alloc] initWithFormat:@"%u", (UInt32)[ud doubleForKey:@"accountID"]];
    self.accountName.text = [ud stringForKey:@"accountName"];
    
    NSString *strPic = [ud stringForKey:@"accountPic"];
    [self.accountPic setImage:[UIImage imageNamed:strPic]];
    
    NSInteger avConfigPreset = [ud integerForKey:@"avConfigPreset"];
    [self updateLiveQualityDetails:avConfigPreset];
    
    NSInteger rowSelected = avConfigPreset;
    if (rowSelected < 0) {
        //自定义参数时，列表项为最后一项
        rowSelected = listPresetLiveQuality.count-1;
    }
    [self.liveQuality selectRow:rowSelected inComponent:0 animated:false];
}

- (void)generateAcount{
    
    UInt32 nRandomID = arc4random();
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    NSString *strAccountName = [ud stringForKey:@"accountName"];
    if (strAccountName == nil) {
        strAccountName = [[NSString alloc] initWithFormat:@"user%u", nRandomID%1000];
        [ud setValue:strAccountName forKey:@"accountName"];
    }
    [ud setDouble:(double)nRandomID forKey:@"accountID"];
    
    int nHeadIndexBase = 86, nHeadIndexEnd = 132;
    NSString *strPic = [[NSString alloc] initWithFormat:@"emoji_%03u.png", nRandomID%(nHeadIndexEnd-nHeadIndexBase+1)+nHeadIndexBase];
    [ud setValue:strPic forKey:@"accountPic"];
}

- (void)presetLiveQuality{
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    
    [ud setInteger:ZegoAVConfigPreset_Generic forKey:@"avConfigPreset"];
}

- (void)updateLiveQualityDetails:(NSInteger)preset{
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    NSInteger nResolutionEnum;
    NSInteger nFPS;
    NSInteger nBitrate;
    CGSize szResolution;
    
    if (preset >= 0) {
        
        ZegoAVConfig *zegoAVConfig= [ZegoAVConfig defaultZegoAVConfig:(ZegoAVConfigPreset)preset];
        
        nResolutionEnum = 0;
        szResolution = [zegoAVConfig getVideoResolution];
        switch ((int)szResolution.width) {
                case 320:
                nResolutionEnum = 0;
                break;
                case 352:
                nResolutionEnum = 1;
                break;
                case 640:
                nResolutionEnum = 2;
                break;
                case 960:
                nResolutionEnum = 3;
                break;
                case 1280:
                nResolutionEnum = 4;
                break;
                case 1920:
                nResolutionEnum = 5;
                break;
                
            default:
                nResolutionEnum = -1;
                break;
        }
        
        nFPS = [zegoAVConfig getVideoFPS];
        nBitrate = [zegoAVConfig getVideoBitrate];
        
        [ud setInteger:nResolutionEnum forKey:@"avConfigResolution"];
        [ud setInteger:nFPS forKey:@"avConfigFPS"];
        [ud setInteger:nBitrate forKey:@"avConfigBitrate"];
        
        [ud setInteger:preset forKey:@"avConfigPreset"];
    }
    else{
        //自定义各种参数
        nResolutionEnum = [ud integerForKey:@"avConfigResolution"];
        nFPS = [ud integerForKey:@"avConfigFPS"];
        nBitrate = [ud integerForKey:@"avConfigBitrate"];
        
        ZegoAVConfig *zegoAVConfig= [ZegoAVConfig defaultZegoAVConfig:0];
        [zegoAVConfig setVideoResolution:(ZegoAVConfigVideoResolution)nResolutionEnum];
        
        szResolution = [zegoAVConfig getVideoResolution];
    }
    
    
    //Update UI
    self.sliderResolution.value = nResolutionEnum;
    self.sliderFPS.value = nFPS;
    self.sliderBitrate.value = nBitrate;
    
    self.labelResolution.text = [[NSString alloc] initWithFormat:@"%ld x %ld", (long)szResolution.width, (long)szResolution.height];
    self.labelFPS.text = [[NSString alloc] initWithFormat:@"%ld", (long)nFPS];
    self.labelBitrate.text = [[NSString alloc] initWithFormat:@"%ld", (long)nBitrate];
}
- (IBAction)clickInside:(id)sender
{
    nHitCount++;
    if (nHitCount >= 5)
    {
        self.tfIP.hidden = false;
        self.tfPort.hidden = false;
        self.tfUrl.hidden = false;
        self.tfAppID.hidden = false;
        self.tfSign.hidden = false;
    }
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 5 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        nHitCount = 0;
    });
}

- (IBAction)sliderResolutionChanged:(UISlider *)sender {
    ZegoAVConfig *zegoAVConfig= [ZegoAVConfig defaultZegoAVConfig:0];
    [zegoAVConfig setVideoResolution:(int)sender.value];
    
    CGSize szResolution = [zegoAVConfig getVideoResolution];
    
    self.labelResolution.text = [[NSString alloc] initWithFormat:@"%ld x %ld", (long)szResolution.width, (long)szResolution.height];
    
    int row = (int)listPresetLiveQuality.count-1;
    [self.liveQuality selectRow:row inComponent:0 animated:true];
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setInteger:(int)sender.value forKey:@"avConfigResolution"];
    [ud setInteger:-1 forKey:@"avConfigPreset"];
}

- (IBAction)sliderFPSChanged:(UISlider *)sender {
    self.labelFPS.text = [[NSString alloc] initWithFormat:@"%d", (int)sender.value];
    
    int row = (int)listPresetLiveQuality.count-1;
    [self.liveQuality selectRow:row inComponent:0 animated:true];
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setInteger:(int)sender.value forKey:@"avConfigFPS"];
    [ud setInteger:-1 forKey:@"avConfigPreset"];
}

- (IBAction)sliderBitrateChanged:(UISlider *)sender {
    self.labelBitrate.text = [[NSString alloc] initWithFormat:@"%d", (int)sender.value];
    
    int row = (int)listPresetLiveQuality.count-1;
    [self.liveQuality selectRow:row inComponent:0 animated:true];
    
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setInteger:(int)sender.value forKey:@"avConfigBitrate"];
    [ud setInteger:-1 forKey:@"avConfigPreset"];
}

- (IBAction)btnChangePicClicked:(UIButton *)sender {
    [self generateAcount];
    [self loadUserDefaults];
}

- (IBAction)viewTouchDown:(UIControl *)sender {
    [[UIApplication sharedApplication] sendAction:@selector(resignFirstResponder) to:nil from:nil forEvent:nil];
}

- (IBAction)textEditingDidEnd:(UITextField *)sender {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setValue:sender.text forKey:@"accountName"];
}
@end
