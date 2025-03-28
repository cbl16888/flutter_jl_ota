#import "FlutterJlOtaPlugin.h"
#import "SNOtaTool.h"

@interface FlutterJlOtaPlugin ()
@property(nonatomic, strong) FlutterMethodChannel *channel;
@end

@implementation FlutterJlOtaPlugin
+ (void)registerWithRegistrar:(NSObject <FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel = [FlutterMethodChannel
            methodChannelWithName:@"flutter_jl_ota"
                  binaryMessenger:[registrar messenger]];
    FlutterJlOtaPlugin *instance = [[FlutterJlOtaPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];

    instance.channel = channel;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    if ([@"getPlatformVersion" isEqualToString:call.method]) {
        result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
    } else if ([@"sn_updateFirmware" isEqualToString:call.method]) { // 固件升级
        NSDictionary *params = call.arguments;
        if ([params isKindOfClass:[NSDictionary class]]) {
            NSString *uuid = params[@"uuid"];
            NSString *filePath = params[@"filePath"];
            if ([uuid isKindOfClass:[NSString class]] &&
                [filePath isKindOfClass:[NSString class]]) {
                [[SNOtaTool shareOtaTool] sn_initActionWithUuidString:uuid filePath:filePath];

                __weak typeof(self) weakSelf = self;
                [[SNOtaTool shareOtaTool] setSn_otaToolProgressCallBack:^(NSInteger progress) {

                    // // 在这里处理进度值
                    [weakSelf.channel invokeMethod:@"progress" arguments:@(progress)];
                }];
            }
        }
    } else {
        result(FlutterMethodNotImplemented);
    }
}

@end
