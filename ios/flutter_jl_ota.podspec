#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_jl_ota.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_jl_ota'
  s.version          = '0.0.1'
  s.summary          = 'Flutter JL OTA.'
  s.description      = <<-DESC
  Plugin for JL OTA.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*.{h,m,swift}'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'

  s.framework = "JL_OTALib"

  s.ios.vendored_frameworks = [
    'Framework/JL_OTALib.framework',
    'Framework/JL_AdvParse.framework',
    'Framework/JL_HashPair.framework',
    'Framework/JL_BLEKit.framework',
    'Framework/DFUnits.framework'
  ]

  s.vendored_frameworks = [
    'JL_OTALib.framework',
    'JL_AdvParse.framework',
    'JL_HashPair.framework',
    'JL_BLEKit.framework',
    'DFUnits.framework'
  ]

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end
