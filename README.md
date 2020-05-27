# [EMedia 让相册、拍照、视频.、预览更方便](https://github.com/JustGank/EMedia)

## 简介

EMedia是一个基于Android系统的媒体文件选择帮助类。

其中主要的功能有

 1. 相册：支持单选、多选、按文件夹分类、系统预览、自定义预览、是否返回原图等操作。
 2. 拍照：使用系统相机，拍照后按照指定路径返回照片。
 3. 录像：支持系统录像，和自定义相机录像，自定义相机支持分辨率，闪光灯最大，最小录制时间等常见功能。
 4. 选取文件：快速调用系统图片选择器。
 5. 返回解析：根据请求的内容，封装返回解析工具，返回可用的数据结果。
 6. 自动获取Provider权限。
 
### 一、调用相册：

```java
new EPickerBuilder(this)
.setPickerType(EPickerBuilder.PickerType.PHOTO_VIDEO) //选择的内容支持：图片 视频 图片和视频
.setMaxChoseNum(9) //最大选择数
.setFilterPhotoMaxSize(10) //图片文件的大小限制
.setProgressDialogClass(ProgressDialog.class) //压缩时，自定义Progress样式
.openCompress(true, cacheDirPathCompress) //压缩文件的输出文件地址
.overSizeVisible(true) //超过大小的文件是否课件
.setOpenPreview(true)  //是否可以预览
.setOpenSkipMemoryCache(true) //是否跳过内存缓存
.setOpenBottomMoreOperate(true) //是否开启按文件夹显示集合
.setPreviewActivity(null)  //支持自定义预览Activity
.startPicker();
```

其他更多功能可以参考： **EPickerBuilder**

实现效果


![在这里插入图片描述](https://img-blog.csdnimg.cn/2020052715315996.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTA0NTE5OTA=,size_16,color_FFFFFF,t_70#pic_center)


**注：Titlebar和主题颜色演示可以自定义的。**

### 二、拍照
拍照部分封装了调用系统拍照：

```java
filePath = IntentUtil.makePhoto(this, cacheDirPathImage);
```

 - 第一个参数为上下文
 - 第二个参数为输出文件的地址
 - 返回的结果为照片的路径。

### 三、调用系统录像

```java
filePath = IntentUtil.makeVideo(this, cacheDirPathVideos);
```

请求参数和返回内容和拍照一样。

### 四、调用自定义录像

目前自定义录像支持：闪光灯、分辨率、翻转摄像头、最小录制时间、最长录制时间、防抖、录像后预览等功能。


![在这里插入图片描述](https://img-blog.csdnimg.cn/20200527153906956.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTA0NTE5OTA=,size_16,color_FFFFFF,t_70#pic_center)


启动样例：
```java
 new ERecordBuilder(MainActivity.this)
                        .setRecordMinTime(3)
                        .setLimitTime(0)
                        .setQuality(ERecordBuilder.RecordQuality.ALL)
                        .setShowLight(true)
                        .setShowRatio(true)
                        .setPreOnClickListener(RecordPreOnClickListener.class)
                        .startRecord(cacheDirPathVideos);
```

更多功能可以参考：**ERecordBuilder**

### 五、调用系统文件

```java
IntentUtil.openFileManager(this);
```


### 六、解析返回值
请求时，内置了默认的请求码，当然也可以自定义请求码，构造器会保存，所以onActivityResult可以通过构造器getRequestCode()方法来区分返回类型。

**其中相册返回的是列表，如果单选时，列表中只有一个元素。**

```java
 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "  requestCode=" + requestCode + "    resultCode=" + resultCode + " data is null=" + (data == null));
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        File temp;
        if (requestCode == EPickerBuilder.getRequestCode()) {
            List<MediaPickerBean> mediaList = IntentUtil.parserMediaResultData(requestCode, data);
            for (int i = 0; i < mediaList.size(); i++) {
                Log.e(TAG, mediaList.get(i).getMediaFilePath());
            }
        } else if (requestCode == IntentUtil.TAKE_PHOTO_REQUEST_CODE) {
            temp = IntentUtil.parserTakedPhoto(this, true);
            if (temp != null) {
                Log.e(TAG, temp.exists() ? "Image take success,file path:" + temp.getAbsolutePath() : "Image file not exist!");
                PicUtils.readPictureDegree(temp.getAbsolutePath());
            }
        } else if (requestCode == IntentUtil.TAKE_VIDEO_REQUEST_CODE) {
            temp = IntentUtil.parserTakedVideo(this, true);
            if (temp != null)
                Log.e(TAG, temp.exists() ? "Video record success,file path is:" + temp.getAbsolutePath() : "Video file not exist!");
        } else if (requestCode == ERecordBuilder.getRequestCode()) {
            temp = IntentUtil.parserCustomTakedVideo(this, data, true);
            if (temp != null)
                Log.e(TAG, temp.exists() ? "Custom video record success,file path is:" + temp.getAbsolutePath() : "Custom video file not exist!");
        } else if (requestCode == IntentUtil.getTakeFileRequestCode()) {
            if (FileChooseUtil.isDownloadsDocument(data.getData())) {
                Toast.makeText(this, "无效文件", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "  Authority = " + data.getData().getAuthority());
                String filePath = FileChooseUtil.getPathFromUri(this, data.getData());
                Log.e(TAG, filePath + "  Authority=" + data.getData().getAuthority());
            }
        }
    }
```
