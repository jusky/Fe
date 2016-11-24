/**
 * Use BaiduMapApi.
 */
var map = new BMap.Map("container");          // 创建地图实例
var point = new BMap.Point(114.364,30.545);  // 创建点坐标
map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别
map.addControl(new BMap.NavigationControl()); // 添加平移缩放控件
map.addControl(new BMap.ScaleControl());       // 添加比例尺控件
map.enableScrollWheelZoom(true);               // 开启鼠标滚轮缩放
var mapWforGPS = new BMapLib.MapWrapper(map, BMapLib.COORD_TYPE_GPS); //由于中国大陆对电子地图进行了偏移，进行GPS坐标转换
function photoInfo(lat, lng, date, file) {
    this.lat = lat;     // 纬度
    this.lng = lng;     // 经度
    this.date = date;   // 拍摄日期
    this.file = file;   // 文件名
}
function addMarker(photoInfo) {
    var marker = new BMap.Marker(new BMap.Point(photoInfo.lng, photoInfo.lat)); // 确立标志点
    var sContent =
        "<div><h4 style='margin:0 0 5px 0; padding:0.2em 0'>" + photoInfo.file + "</h4>"
    +   "<img style='float: right;margin: 4px' id='imgDemo' src='file://" + photoInfo.file + "' width='140' height='105' />"
    +   "<p style='margin:0;line-height: 1.5;font-size: 13px;text-indent: 2em'>拍摄时间：" + photoInfo.date + "</p>"
    +   "</div>";                                                               // 信息窗口内容
    var infoWindow = new BMap.InfoWindow(sContent);
    mapWforGPS.addOverlay(marker);
    marker.addEventListener("click", function () {
        this.openInfoWindow(infoWindow);
        document.getElementById('imgDemo').onload = function () {
            infoWindow.redraw();    // 在网络状况不佳时，图片加载完毕后再进行重绘
        }
    });
}
