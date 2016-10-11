//
//  ZegoAdvancedTableViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/30.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoAdvancedTableViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoSettings.h"

@interface ZegoAdvancedTableViewController () <UITextFieldDelegate, UITextViewDelegate>

@property (weak, nonatomic) IBOutlet UITextField *appID;
@property (weak, nonatomic) IBOutlet UITextView *appSign;

@property (weak, nonatomic) IBOutlet UISwitch *testEnvSwitch;
@property (weak, nonatomic) IBOutlet UISwitch *hardwareAcceleratedSwitch;
@property (weak, nonatomic) IBOutlet UISwitch *mixStreamSwitch;

@property (nonatomic, strong) UITapGestureRecognizer *tapGesture;

@end

@implementation ZegoAdvancedTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    self.testEnvSwitch.on = isUseingTestEnv();
    self.hardwareAcceleratedSwitch.on = ZegoIsRequireHardwareAccelerated();
    
    if (ZegoGetAppID() != 0) {
        [self.appID setText:[NSString stringWithFormat:@"%u", ZegoGetAppID()]];
    }
    
    UITapGestureRecognizer *gesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onAlphaEnv:)];
    gesture.numberOfTapsRequired = 5;
    [self.tableView addGestureRecognizer:gesture];
}

- (void)onAlphaEnv:(UIGestureRecognizer *)gesture
{
    BOOL alpha = isUsingAlphaEnv();
    setUseAlphaEnv(!alpha);
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"测试环境" message:alpha ? @"关闭Alpha环境" : @"打开Alpha环境" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];
    [alert show];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {    
    if (self.appID.text.length > 0 && self.appSign.text.length > 0)
    {
        NSString *strAppID = self.appID.text;
        NSUInteger appID = (NSUInteger)[strAppID longLongValue];
        ZegoDemoSetCustomAppIDAndSign((uint32)appID, self.appSign.text);
    }
    
    setUseTestEnv(self.testEnvSwitch.on);
    
    [super viewWillDisappear:animated];
}

- (IBAction)toggleTestEnv:(id)sender {
    UISwitch *s = (UISwitch *)sender;
    setUseTestEnv(s.on);
}

- (IBAction)toggleHardwareAccelerated:(id)sender {
    UISwitch *s = (UISwitch *)sender;
    ZegoRequireHardwareAccelerated(s.on);
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    if (!self.appID.isEditing && ![self.appSign isFirstResponder])
    {
        [self.view endEditing:YES];
    }
}

- (void)onTapTableView:(UIGestureRecognizer *)gesture
{
    [self.view endEditing:YES];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    
    return YES;
}

- (void)textFieldDidBeginEditing:(UITextField *)textField
{
    if (self.tapGesture == nil)
        self.tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapTableView:)];
    
    [self.tableView addGestureRecognizer:self.tapGesture];
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{
    if (self.tapGesture)
    {
        [self.tableView removeGestureRecognizer:self.tapGesture];
        self.tapGesture = nil;
    }
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text
{
    if ([text isEqualToString:@"\n"])
    {
        [textView resignFirstResponder];
        return NO;
    }
    
    return YES;
}

- (void)textViewDidBeginEditing:(UITextView *)textView
{
    if (self.tapGesture == nil)
        self.tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapTableView:)];
    
    [self.tableView addGestureRecognizer:self.tapGesture];
}

- (void)textViewDidEndEditing:(UITextView *)textView
{
    if (self.tapGesture)
    {
        [self.tableView removeGestureRecognizer:self.tapGesture];
        self.tapGesture = nil;
    }
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
