package tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

import java.io.*;
import java.util.*;

/**
 * Created by yellowsea on 2016/8/9.
 */
public class PhotoAnalyzer {
    public static void main(String[] args) throws ImageProcessingException, IOException {
        write(args[0]);
    }

    public static void write(String path) throws ImageProcessingException, IOException {
        for (File file : getFiles(path)) {
            System.out.println(file);
        }

        writeListToJSON(getPhotoInfoList(getFiles(path)));
    }

    public static List<File> getFiles(String pathname) {
        File path = new File(pathname);
        String[] acceptedExtensions = new String[]{".jpg", ".jpeg"};
        List<File> files = new ArrayList<>();
        File[] currentFiles = path.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    files.addAll(getFiles(pathname.getAbsolutePath()));
                else
                    for (String extension : acceptedExtensions)
                        if (pathname.getName().toLowerCase().endsWith(extension))
                            return true;
                return false;
            }
        });
        files.addAll(Arrays.asList(currentFiles));
        return files;
    }

    public static List<PhotoInfo> getPhotoInfoList(List<File> files) throws ImageProcessingException, IOException {
        List<PhotoInfo> photoInfoList = new ArrayList<>();
        for (File file : files) {
            // Read all metadata from the image
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            // See whether it has GPS data
            Collection<GpsDirectory> gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory.class);
            if (gpsDirectories == null)
                continue;
            for (GpsDirectory gpsDirectory : gpsDirectories) {
                GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                if (geoLocation != null && !geoLocation.isZero()) {
                    Date date = gpsDirectory.getGpsDate();
                    String filename = file.getAbsolutePath();
                    photoInfoList.add(new PhotoInfo(geoLocation, date, filename));
                    break;
                }
            }
        }
        return photoInfoList;
    }

    public static void writeListToJSON(List<PhotoInfo> photoInfoList) throws FileNotFoundException {
        PrintStream ps = new PrintStream(new File("WebRoot/photoinfo.js"));
        ps.print("var photoInfoList = ");
        ps.print(JSON.toJSONString(photoInfoList));
        ps.println(";");
        ps.flush();
    }

    public static class PhotoInfo {
        private final GeoLocation location;
        private final Date date;
        private final String filename;
        public PhotoInfo(final GeoLocation location, final Date date, final String filename) {
            this.location = location;
            this.date = date;
            this.filename = filename;
        }

        public Date getDate() {
            return date;
        }

        public GeoLocation getLocation() {
            return location;
        }

        public String getFilename() {
            return filename;
        }
    }
}
