//
//  ZegoRenderViewController.m
//  LiveDemo3
//
//  Created by Strong on 2016/10/18.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoRenderViewController.h"
#import "ZegoAVKitManager.h"
#import <OpenGLES/EAGL.h>
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>

@interface ZegoRenderViewController () <ZegoLiveApiRenderDelegate>

@end

@implementation ZegoRenderViewController
{
    CVOpenGLESTextureCacheRef m_pTexCache;
    GLuint m_hProgram;
    GLuint m_hVertexShader;
    GLuint m_hFragShader;
    int m_nFrameUniform;
    GLfloat m_lstVertices[8];
    GLfloat m_lstTexCoord[8];
    
    enum {
        ATTRIB_POSITION = 0,
        ATTRIB_TEXCOORD = 1
    };
    
    bool is_inited_;
    
    CVPixelBufferPoolRef pool_;
//    CVPixelBufferRef head_pixel_buffer_;
    int video_width_;
    int video_height_;
    
    dispatch_semaphore_t signal_;
    NSMutableArray<NSValue *>* array_;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    is_inited_ = false;

    [getZegoAV_ShareInstance() setRenderDelegate:self];
    
    signal_ = dispatch_semaphore_create(1);
    array_ = [[NSMutableArray alloc] initWithCapacity:4];
    
    // Create an OpenGL ES context and assign it to the view loaded from storyboard
    GLKView *view = (GLKView *)self.view;
    view.context = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
    view.drawableDepthFormat = GLKViewDrawableDepthFormatNone;
    view.drawableStencilFormat = GLKViewDrawableStencilFormatNone;
    
    // Set animation frame rate
    self.preferredFramesPerSecond = 30;
    
    // Not shown: load shaders, textures and vertex arrays, set up projection matrix
    [self setupGL];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void) setupGL {
    m_lstVertices[0] = -1.0;
    m_lstVertices[1] = -1.0;
    
    m_lstVertices[2] = 1.0;
    m_lstVertices[3] = -1.0;
    
    m_lstVertices[4] = -1.0;
    m_lstVertices[5] = 1.0;
    
    m_lstVertices[6] = 1.0;
    m_lstVertices[7] = 1.0;
    
    // * texcoord
    m_lstTexCoord[0] = 0.0;
    m_lstTexCoord[1] = 1.0;
    
    m_lstTexCoord[2] = 1.0;
    m_lstTexCoord[3] = 1.0;
    
    m_lstTexCoord[4] = 0.0;
    m_lstTexCoord[5] = 0.0;
    
    m_lstTexCoord[6] = 1.0;
    m_lstTexCoord[7] = 0.0;
}

- (void)initializeGLContext:(EAGLContext *)context {
    if (is_inited_) {
        return ;
    }
    is_inited_ = true;
    
    [EAGLContext setCurrentContext:context];
    
    // * disable depth test
    glDisable(GL_DEPTH_TEST);
    
    // * create our texture cache
    CVOpenGLESTextureCacheCreate(kCFAllocatorDefault, NULL, context, NULL, &m_pTexCache);
    
    // * create our program
    m_hProgram = glCreateProgram();
    
    // * compile vertex shader
    const static char *strVertexShader = " \
    attribute vec4 position; \
    attribute mediump vec4 texcoord; \
    \
    varying mediump vec2 textureCoordinate; \
    \
    void main() \
    { \
    gl_Position = position; \
    textureCoordinate = texcoord.xy; \
    } \
    ";
    m_hVertexShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(m_hVertexShader, 1, &strVertexShader, 0);
    glCompileShader(m_hVertexShader);
    GLint status;
    glGetShaderiv(m_hVertexShader, GL_COMPILE_STATUS, &status);
    if (status != GL_TRUE) {
        // * compile shader error
        // * to_do: handle error
    }
    
    // * compile fragment shader
    const static char *strFragmentShader = " \
    varying highp vec2 textureCoordinate; \
    uniform sampler2D frame; \
    \
    void main() \
    { \
    gl_FragColor = texture2D(frame, textureCoordinate); \
    } \
    ";
    m_hFragShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(m_hFragShader, 1, &strFragmentShader, 0);
    glCompileShader(m_hFragShader);
    // * to_do: check status
    
    // * attach shader to program
    glAttachShader(m_hProgram, m_hVertexShader);
    glAttachShader(m_hProgram, m_hFragShader);
    
    // * bind attributes
    static const char * ATTRIB_NAMES[] = {
        "position",
        "texcoord"
    };
    glBindAttribLocation(m_hProgram, ATTRIB_POSITION, ATTRIB_NAMES[ATTRIB_POSITION]);
    glBindAttribLocation(m_hProgram, ATTRIB_TEXCOORD, ATTRIB_NAMES[ATTRIB_TEXCOORD]);
    
    // * link program
    glLinkProgram(m_hProgram);
    glGetProgramiv(m_hProgram, GL_LINK_STATUS, &status);
    if (status != GL_TRUE) {
        // * to_do: handle error
    }
    
    // * get uniform index
    m_nFrameUniform = glGetUniformLocation(m_hProgram, "frame");
    
    // * use program
    glUseProgram(m_hProgram);
    
    // * enable attributes
    glEnableVertexAttribArray(ATTRIB_POSITION);
    glEnableVertexAttribArray(ATTRIB_TEXCOORD);
    
}

- (void)uninitializeGLContext:(EAGLContext *)context {
    if (!is_inited_) {
        return ;
    }
    is_inited_ = false;
    
    EAGLContext *pOldGLContext = nil;
    if ([EAGLContext currentContext] != context) {
        pOldGLContext = [EAGLContext currentContext];
        [EAGLContext setCurrentContext:context];
    }
    
    glDeleteShader(m_hFragShader);
    glDeleteShader(m_hVertexShader);
    glDeleteProgram(m_hProgram);
    
    if (m_pTexCache) {
        CFRelease(m_pTexCache);
        m_pTexCache = 0;
    }
    
    // * restore context
    if (pOldGLContext != nil) {
        [EAGLContext setCurrentContext:pOldGLContext];
    }
}

// Set shader uniforms to values calculated in -update
- (void)update {
    //    _rotation += self.timeSinceLastUpdate * M_PI_2; // one quarter rotation per second
    //
    //    // Set up transform matrices for the rotating planet
    //    GLKMatrix4 modelViewMatrix = GLKMatrix4MakeRotation(_rotation, 0.0f, 1.0f, 0.0f);
    //    _normalMatrix = GLKMatrix3InvertAndTranspose(GLKMatrix4GetMatrix3(modelViewMatrix), NULL);
    //    _modelViewProjectionMatrix = GLKMatrix4Multiply(_projectionMatrix, modelViewMatrix);
}

- (void)glkView:(GLKView *)view drawInRect:(CGRect)rect {
    CVPixelBufferRef buf = [self getConsumerPixelBuffer];
    if (!buf) {
        return ;
    }
    
    [self initializeGLContext:view.context];
    
    EAGLContext *pOldGLContext = nil;
    if ([EAGLContext currentContext] != view.context) {
        pOldGLContext = [EAGLContext currentContext];
        [EAGLContext setCurrentContext:view.context];
    }
    
    int width = (int)CVPixelBufferGetWidth(buf);
    int height = (int)CVPixelBufferGetHeight(buf);
    
    int view_width_;
    int view_height_;
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_WIDTH, &view_width_);
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_HEIGHT, &view_height_);
    
    CVOpenGLESTextureRef texture = NULL;
    CVReturn err = CVOpenGLESTextureCacheCreateTextureFromImage(kCFAllocatorDefault,
                                                                m_pTexCache,
                                                                buf,
                                                                NULL,
                                                                GL_TEXTURE_2D,
                                                                GL_RGBA,
                                                                (GLsizei)width,
                                                                (GLsizei)height,
                                                                GL_BGRA,
                                                                GL_UNSIGNED_BYTE,
                                                                0,
                                                                &texture);
    
    if (err == kCVReturnSuccess && texture) {
        glViewport(0, 0, view_width_, view_height_);
        glUseProgram(m_hProgram);
        
        glClearColor(0, 1.0f, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(CVOpenGLESTextureGetTarget(texture), CVOpenGLESTextureGetName(texture));
        glUniform1i(m_nFrameUniform, 0);
        
        // Set texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        glVertexAttribPointer(ATTRIB_POSITION, 2, GL_FLOAT, 0, 0, m_lstVertices);
        glVertexAttribPointer(ATTRIB_TEXCOORD, 2, GL_FLOAT, 0, 0, m_lstTexCoord);
        
        glEnableVertexAttribArray(ATTRIB_POSITION);
        glEnableVertexAttribArray(ATTRIB_TEXCOORD);
        
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        
        glBindTexture(CVOpenGLESTextureGetTarget(texture), 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        CFRelease(texture);
    }
    CFRelease(buf);
    
    if (pOldGLContext != nil) {
        [EAGLContext setCurrentContext:pOldGLContext];
    }
}

- (CVPixelBufferRef)getConsumerPixelBuffer {
    __block CVPixelBufferRef pixelBuffer = NULL;
    dispatch_time_t overTime = dispatch_time(DISPATCH_TIME_NOW, 3 * NSEC_PER_SEC);
    dispatch_sync(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        dispatch_semaphore_wait(signal_, overTime);
        
        if (array_.count > 0) {
            NSValue* value = [array_ objectAtIndex:0];
            pixelBuffer = value.pointerValue;
            [array_ removeObjectAtIndex:0];
        }
        
        dispatch_semaphore_signal(signal_);
    });
    return pixelBuffer;
}

- (void)createPixelBufferPool{
    NSDictionary *pixelBufferAttributes = [NSDictionary dictionaryWithObjectsAndKeys:
                                           [NSNumber numberWithBool:YES], (id)kCVPixelBufferOpenGLCompatibilityKey,
                                           [NSNumber numberWithInt:video_width_], (id)kCVPixelBufferWidthKey,
                                           [NSNumber numberWithInt:video_height_], (id)kCVPixelBufferHeightKey,
                                           [NSDictionary dictionary], (id)kCVPixelBufferIOSurfacePropertiesKey,
                                           [NSNumber numberWithInt:kCVPixelFormatType_32BGRA], (id)kCVPixelBufferPixelFormatTypeKey,
                                           nil
                                           ];
    
    CFDictionaryRef ref = (__bridge CFDictionaryRef)pixelBufferAttributes;
    CVReturn ret = CVPixelBufferPoolCreate(nil, nil, ref, &pool_);
    if (ret != kCVReturnSuccess) {
        return ;
    }
}

#pragma mark ZegoLiveRenderDelegate
- (CVPixelBufferRef)onCreateInputBufferWithWidth:(int)width height:(int)height stride:(int)stride
{
    if (video_width_ != width || video_height_ != height)
    {
        if (video_height_ && video_width_)
        {
            CVPixelBufferPoolFlushFlags flag = 0;
            CVPixelBufferPoolFlush(pool_, flag);
            CFRelease(pool_);
            pool_ = nil;
        }
        
        video_width_ = width;
        video_height_ = height;
        [self createPixelBufferPool];
    }
    
    CVPixelBufferRef pixelBuffer;
    CVReturn ret = CVPixelBufferPoolCreatePixelBuffer(nil, pool_, &pixelBuffer);
    if (ret != kCVReturnSuccess)
        return nil;
    
    return pixelBuffer;
}

- (void)onPixelBufferCopyed:(CVPixelBufferRef)pixelBuffer index:(RemoteViewIndex)index
{
    dispatch_time_t overTime = dispatch_time(DISPATCH_TIME_NOW, 3 * NSEC_PER_SEC);
    dispatch_sync(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        dispatch_semaphore_wait(signal_, overTime);
        
        [array_ addObject:[NSValue valueWithPointer:pixelBuffer]];
        
        dispatch_semaphore_signal(signal_);
    });
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
