package com.nexusplay.service;

import com.nexusplay.entity.User;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.bytedeco.opencv.global.opencv_core.NORM_MINMAX;
import static org.bytedeco.opencv.global.opencv_core.normalize;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.HISTCMP_CORREL;
import static org.bytedeco.opencv.global.opencv_imgproc.calcHist;
import static org.bytedeco.opencv.global.opencv_imgproc.compareHist;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.equalizeHist;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

public class FaceRecognitionService {

    private static final String CASCADE_RESOURCE = "/haar/haarcascade_frontalface_default.xml";

    private final CascadeClassifier faceDetector;
    private final OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
    private final JavaFXFrameConverter fxConverter = new JavaFXFrameConverter();

    private OpenCVFrameGrabber grabber;
    private volatile boolean running = false;

    public FaceRecognitionService() {
        String cascadePath = extractResourceToTempFile(CASCADE_RESOURCE).getAbsolutePath();
        this.faceDetector = new CascadeClassifier(cascadePath);
        if (this.faceDetector.isNull() || this.faceDetector.empty()) {
            throw new IllegalStateException("Failed to load Haar cascade: " + CASCADE_RESOURCE);
        }
    }

    public void startCamera(Consumer<Image> frameCallback, Consumer<String> statusCallback) {
        Objects.requireNonNull(frameCallback, "frameCallback");
        Objects.requireNonNull(statusCallback, "statusCallback");

        running = true;

        Thread t = new Thread(() -> {
            try {
                grabber = new OpenCVFrameGrabber(0);
                grabber.start();

                while (running) {
                    Frame frame;
                    synchronized (this) {
                        frame = grabber.grab();
                    }
                    if (frame == null) {
                        continue;
                    }

                    Mat mat = matConverter.convert(frame);
                    if (mat == null || mat.empty()) {
                        continue;
                    }

                    RectVector faces = detectFaces(mat);
                    for (long i = 0; i < faces.size(); i++) {
                        Rect face = faces.get(i);
                        rectangle(mat, face, new Scalar(255, 77, 46, 255), 2, 8, 0);
                    }

                    Image fxImage = fxConverter.convert(frame);
                    Platform.runLater(() -> {
                        frameCallback.accept(fxImage);
                        statusCallback.accept(faces.size() > 0
                                ? "Face detected! Analyzing..."
                                : "Position your face in the frame...");
                    });

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> statusCallback.accept("Camera error: " + e.getMessage()));
            } finally {
                stopGrabberQuietly();
            }
        }, "camera-thread");
        t.setDaemon(true);
        t.start();
    }

    public User recognizeFace(List<User> usersWithPhotos) {
        if (usersWithPhotos == null || usersWithPhotos.isEmpty()) {
            return null;
        }
        if (grabber == null) {
            return null;
        }

        try {
            Frame frame;
            synchronized (this) {
                frame = grabber.grab();
            }
            if (frame == null) {
                return null;
            }

            Mat liveMat = matConverter.convert(frame);
            if (liveMat == null || liveMat.empty()) {
                return null;
            }

            Mat liveFace = extractLargestFaceGray(liveMat);
            if (liveFace == null) {
                return null;
            }

            for (User user : usersWithPhotos) {
                String picPath = user == null ? null : user.getProfilePicture();
                if (picPath == null || picPath.isBlank()) {
                    continue;
                }
                String path = picPath.trim();
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    continue; // current implementation supports local file paths only
                }

                Mat storedMat = imread(path);
                if (storedMat == null || storedMat.empty()) {
                    continue;
                }

                Mat storedFace = extractLargestFaceGray(storedMat);
                if (storedFace == null) {
                    continue;
                }

                double similarity = compareFaceSimilarity(liveFace, storedFace);
                if (similarity >= 0.85) {
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void stopCamera() {
        running = false;
        stopGrabberQuietly();
    }

    private RectVector detectFaces(Mat bgrMat) {
        Mat gray = new Mat();
        cvtColor(bgrMat, gray, COLOR_BGR2GRAY);
        equalizeHist(gray, gray);

        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(80, 80), new Size());
        return faces;
    }

    private Mat extractLargestFaceGray(Mat bgrMat) {
        Mat gray = new Mat();
        cvtColor(bgrMat, gray, COLOR_BGR2GRAY);
        equalizeHist(gray, gray);

        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(80, 80), new Size());
        if (faces.size() == 0) {
            return null;
        }

        Rect best = faces.get(0);
        long bestArea = (long) best.width() * (long) best.height();
        for (long i = 1; i < faces.size(); i++) {
            Rect r = faces.get(i);
            long area = (long) r.width() * (long) r.height();
            if (area > bestArea) {
                best = r;
                bestArea = area;
            }
        }

        Mat faceRoi = new Mat(gray, best);
        Mat face = new Mat();
        resize(faceRoi, face, new Size(200, 200));
        return face;
    }

    private double compareFaceSimilarity(Mat aGray200, Mat bGray200) {
        // Histogram correlation on normalized grayscale face crops (simple + fast baseline).
        Mat histA = new Mat();
        Mat histB = new Mat();

        int[] channels = {0};
        int[] histSize = {256};
        float[] ranges = {0f, 256f};

        calcHist(aGray200, 1, channels, new Mat(), histA, 1, histSize, ranges, true, false);
        calcHist(bGray200, 1, channels, new Mat(), histB, 1, histSize, ranges, true, false);

        normalize(histA, histA, 0, 1, NORM_MINMAX, -1, new Mat());
        normalize(histB, histB, 0, 1, NORM_MINMAX, -1, new Mat());

        return compareHist(histA, histB, HISTCMP_CORREL);
    }

    private static File extractResourceToTempFile(String resourcePath) {
        try (InputStream in = FaceRecognitionService.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource: " + resourcePath);
            }
            File tmp = File.createTempFile("nexus_haar_", ".xml");
            tmp.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                in.transferTo(out);
            }
            return tmp;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract resource " + resourcePath, e);
        }
    }

    private void stopGrabberQuietly() {
        OpenCVFrameGrabber g = grabber;
        if (g == null) {
            return;
        }
        try {
            g.stop();
        } catch (Exception ignored) {
        }
        try {
            g.release();
        } catch (Exception ignored) {
        }
        grabber = null;
    }
}
