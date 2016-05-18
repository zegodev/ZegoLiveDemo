//
//  ViewController.m
//  LiveDemo2
//
//  Created by Randy Qiu on 4/1/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#import "ViewController.h"
#include "ZegoAVKitManager.h"
#include "ZegoSettingViewController.h"
#include "ZegoLiveViewController.h"

@interface ViewController () <UITextFieldDelegate, UIAlertViewDelegate>

@property (weak, nonatomic) IBOutlet UITextField *liveID;
@property (weak, nonatomic) IBOutlet UITextField *publishTitle;

@property (weak, nonatomic) IBOutlet UIButton *btnPublish;
@property (weak, nonatomic) IBOutlet UIButton *btnPlay;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *bottomSpacingConstraint;

@property (strong) UIGestureRecognizer *tapRecognizer;
@end


@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    if (self.liveID.text.length == 0) {
        [self.liveID setText:@"5180"];
    }
    [self.publishTitle setText:[NSString stringWithFormat:@"Hello-%@", [ZegoSettings sharedInstance].userName]];
    
    self.liveID.delegate = self;
    self.publishTitle.delegate = self;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillDismiss:) name:UIKeyboardWillHideNotification object:nil];
    
    if (self.tapRecognizer == nil) {
        self.tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTouchEvent:)];
        [self.view addGestureRecognizer:self.tapRecognizer];
    }
}


- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}


- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    dispatch_async(dispatch_get_main_queue(), ^{
        // 强制 portrait 显示
        [[UIDevice currentDevice] setValue:@(UIInterfaceOrientationMaskPortrait) forKey:@"orientation"];
    });
    
    NSString *oldPublishingStreamID = [ZegoSettings sharedInstance].publishingStreamID;
    NSString *liveChannel = [ZegoSettings sharedInstance].publishingLiveChannel;
    
    if (oldPublishingStreamID.length > 0 && liveChannel.length > 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Restore Publish?" message:@"Previous live was not ended properly, would you like to restore it?" delegate:self cancelButtonTitle:@"NO" otherButtonTitles:@"YES", nil];
        [alert show];
    }
}


- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    [UIViewController attemptRotationToDeviceOrientation];
}


- (BOOL)shouldAutorotate {
    return YES;
}


- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}


- (void)handleKeyboardWillShow:(NSNotification *)notification {
    CGSize kbSize = [[notification.userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue].size;
    self.bottomSpacingConstraint.constant = kbSize.height;
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}


- (void)handleKeyboardWillDismiss:(NSNotification *)notification {
    self.bottomSpacingConstraint.constant = 0;
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.destinationViewController isKindOfClass:[ZegoLiveViewController class]]) {
        ZegoLiveViewController *c = (ZegoLiveViewController *)segue.destinationViewController;
        if (sender == self.btnPublish) {
            c.liveType = 1;
            c.liveTitle = self.publishTitle.text;
            c.liveChannel = self.liveID.text;
        } else if (sender == self.btnPlay) {
            c.liveType = 2;
            c.liveChannel = self.liveID.text;
        } else if (sender == self) {
            c.liveType = 1;
            c.liveChannel = [ZegoSettings sharedInstance].publishingLiveChannel;
            c.streamID = [ZegoSettings sharedInstance].publishingStreamID;
        }
    }
}

#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.view endEditing:YES];
    return YES;
}



- (void)handleTouchEvent:(id)sender {
    [self.view endEditing:YES];
}

#pragma mark UIAlertViewDelegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    NSLog(@"%s, %ld", __func__, (long)buttonIndex);
    
    if (buttonIndex == 1) { // YES
        [self performSegueWithIdentifier:@"LiveScene" sender:self];
    } else {
        // NO, 丢弃
        [ZegoSettings sharedInstance].publishingStreamID = @"";
        [ZegoSettings sharedInstance].publishingLiveChannel = @"";
    }
}

@end
