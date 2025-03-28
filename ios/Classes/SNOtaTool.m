//
//  SNOtaTool.m
//  jl_ota
//
//  Created by 郑卢峰 on 2024/9/3.
//

#import "SNOtaTool.h"
#import "JL_OTALib/JL_OTALib.h"
#import "JLBleManager.h"

static SNOtaTool *user = nil;

@interface SNOtaTool () <JLBleManagerOtaDelegate>
/** 文件路径 */
@property (nonatomic, copy) NSString *filePath;
/** jl ota */
@property (nonatomic, strong) JL_OTAManager *otaManager;
@end

@implementation SNOtaTool

/**
 *  实现单例类方法
 *
 *  @return 单例对象
 */
+ (instancetype)shareOtaTool {
    if (user == nil) {
        user = [[self alloc] init];
        
        [user setupNotifyAction];
    }
    
    return user;
}

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    // 只执行一次，多用在类方法中用来返回一个单例
    static dispatch_once_t onceToken;
    
    // 该函数接收一个dispatch_once用于检查该代码块是否已经被调度
    // dispatch_once不仅意味着代码仅会被运行一次，而且还是线程安全的
    dispatch_once(&onceToken, ^{
        user = [super allocWithZone:zone];
    });
    
    return user;
}

#pragma mark - 代理
/**
 *  ota升级过程状态回调
 */
- (void)otaProgressWithOtaResult:(JL_OTAResult)result withProgress:(float)progress {
    NSLog(@"sn_log => otaProgressWithOtaResult : %f , result: %d", progress, result);
    NSLog(@"sn_log => JL_OTAResultPreparing ? %d", JL_OTAResultPreparing == result);
    
    // 准备下发中
    if (JL_OTAResultPreparing == result) {
        if (self.sn_otaToolProgressCallBack) {
            int intValue = (int)(progress * 100);
            
            if (intValue >= 100) {
                intValue = 99;
            }
            
            self.sn_otaToolProgressCallBack(intValue);
        }
    } else if (JL_OTAResultSuccess == result) {
        self.sn_otaToolProgressCallBack(100);
    }
}

#pragma mark - 自定义方法
/**
 *  初始化
 */
- (void)sn_initActionWithUuidString:(NSString *)uuidString filePath:(NSString *)filePath {
    
    self.filePath = filePath;
    
    [[JLBleManager sharedInstance] addDelegate:self];
    [[JLBleManager sharedInstance] connectPeripheralWithUUID:uuidString];
}

/**
 *  注册通知
 */
- (void)setupNotifyAction {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(sn_notifyAction:) name:kFLT_BLE_PAIRED object:nil];
}

/**
 *  移除通知
 */
- (void)removeNotifyAction {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark 纸张错误选择通知事件
- (void)sn_notifyAction:(NSNotification *)note {
    NSString *name = note.name;
    
    // 设备注册完成
    if ([name isEqual:kFLT_BLE_PAIRED]) {
//        [[JLBleManager sharedInstance] otaFuncWithFilePath:self.filePath];
        
        __weak typeof(self) weakSelf = self;
        // 获取设备信息
        [[JLBleManager sharedInstance] getDeviceInfo:^(BOOL needForcedUpgrade) {
            NSLog(@"sn_log => needForcedUpgrade : %d", needForcedUpgrade);
            
//            // 延迟操作
//            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                
                [[JLBleManager sharedInstance] otaFuncWithFilePath:weakSelf.filePath];
//            });
        }];
    }
}

@end
