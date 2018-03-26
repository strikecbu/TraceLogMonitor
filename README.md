## TransLog Monitor for IBM WAS
針對WAS產生的trace log做分析查找是否有hang住的log，若超過設定的個數，則會寄送Email and SMS(option) 做提醒。

## About deploy 

### maven打包
maven package後會產生transLogMonitor_pack-1.0.zip 於target中，解壓縮後於**App**資料夾 will see 3 files:

````
1. config.properties - 相關設定於此(路徑不能有"\")
2. TraceLogMonitor-1.0.jar - 主要程式
3. start.bat - 用於windows下直接執行jar

````
### windows service install
資料夾**yajsw-stable-12.11**中的conf資料夾中**wrapper.conf**可設定要包的jar相關資訊，條列要設定的如下：

````
* wrapper.working.dir -打包路徑
* wrapper.ntservice.name -jar name
* wrapper.ntservice.displayname -於window servicey 資訊
* wrapper.ntservice.description -於window servicey 資訊

````
*wrapper.conf亦可透過genConfig.bat產生，詳細參閱<a href="http://yajsw.sourceforge.net/#mozTocId527639">官網</a>*

bat資料夾執行installService.bat，即可將jar打包並安裝於windows中成為一個service。

### windows service uninstall
bat資料夾執行uninstallService.bat即可



