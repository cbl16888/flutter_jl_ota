//
//  OtaTool.h
//

#import <Foundation/Foundation.h>
#import "JLBleManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface OtaTool : NSObject

+ (instancetype)sharedInstance;

- (void)startOtaWithUuid:(NSString *)uuid filePath:(NSString *)filePath;
- (void)cancelOtaUpdate:(void (^)(uint8_t status))completion;
- (void)setOtaProgressCallback:(void (^)(NSInteger progress, NSString *status))callback;

@end

NS_ASSUME_NONNULL_END