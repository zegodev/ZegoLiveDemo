//
//  ViewController.h
//


#import <UIKit/UIKit.h>


extern NSString * const ZegoMovieParameterMinBufferedDuration;    // Float
extern NSString * const ZegoMovieParameterMaxBufferedDuration;    // Float
extern NSString * const ZegoMovieParameterDisableDeinterlacing;   // BOOL
extern NSString * const ZegoMovieParameterScalingMode;          // NSInteger


typedef enum{
    ZegoMovieScalingModeNone,       // No scaling
    ZegoMovieScalingModeAspectFit,  // Uniform scale until one dimension fits
    ZegoMovieScalingModeAspectFill, // Uniform scale until the movie fills the visible bounds. One dimension may have clipped contents
    ZegoMovieScalingModeFill        // Non-uniform scale. Both render dimensions will exactly match the visible bounds
}ZegoMoviePlayerScalingMode;

typedef enum {
    ZegoMoviePlayerStatus_None = 0,
    ZegoMoviePlayerStatus_Loading = 1,
    ZegoMoviePlayerStatus_ReadyToPlay = 2,
    ZegoMoviePlayerStatus_Playing = 3,
    ZegoMoviePlayerStatus_Paused = 4,
    ZegoMoviePlayerStatus_Buffering = 5,
    ZegoMoviePlayerStatus_FinishBuffering = 6,
    ZegoMoviePlayerStatus_Stopping = 7,
    ZegoMoviePlayerStatus_Stopped = 8
}ZegoMoviePlayerStatus;

@protocol ZegoMoviePlayerDelegate <NSObject>
@optional

- (void) onZegoMoviePlayerUpdatePlayerStatus:(ZegoMoviePlayerStatus)status;
- (void) onZegoMoviePlayerDurationAvairable:(CGFloat)duration;
- (void) onZegoMoviePlayerNaturalSizeAvairable:(CGSize)naturalSize;
- (void) onZegoMoviePlayerUpdateProgress:(CGFloat)position playableDuration:(CGFloat)playableDuration;
- (void) onZegoMoviePlayerError:(NSError *)error;

@end

@interface ZegoMoviePlayer:NSObject

+ (id) movieControllerWithContentPath: (NSString *) path
                                presentView: (UIView *)view
                                autoplay: (BOOL)autoplay
                                parameters: (NSDictionary *) parameters;

@property (readonly) BOOL playing;
@property (readonly) CGFloat duration;
@property (readonly) CGSize naturalSize;



- (void) play;
- (void) pause;
- (void) restorePlay;
- (CGFloat) seek:(CGFloat)position;

- (void) setScalingMode:(ZegoMoviePlayerScalingMode)mode;
- (void) setDelegate:(id<ZegoMoviePlayerDelegate>)delegate callBackQueue:(dispatch_queue_t)cbQueue;

- (void) didReceiveMemoryWarning;

@end


