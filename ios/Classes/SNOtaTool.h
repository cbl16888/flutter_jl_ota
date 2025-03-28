//
//  SNOtaTool.h
//  jl_ota
//
//  Created by 郑卢峰 on 2024/9/3.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/** 进度回调 */
typedef void (^SNOtaToolProgressCallBack)(NSInteger progress);

@interface SNOtaTool : NSObject
/**
 *  实现单例类方法
 *
 *  @return 单例对象
 */
+ (instancetype)shareOtaTool;

/** 进度回调 */
@property (nonatomic, copy) SNOtaToolProgressCallBack sn_otaToolProgressCallBack;

/**
 *  初始化
 */
- (void)sn_initActionWithUuidString:(NSString *)uuidString filePath:(NSString *)filePath;
@end

NS_ASSUME_NONNULL_END
