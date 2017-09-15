package com.example.android.emojify;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {

    private static final double EYE_OPEN_THRESHOLD = 0.4;
    private static final double SMILING_THRESHOLD = 0.25;
    private static final double NEUTRAL_THRESHOLD = 0.09;
    private static final float EMOJI_SCALE_FACTOR = 1f;


    static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap image) {
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(false)
                .build();

        Frame frame = new Frame.Builder().setBitmap(image).build();

        SparseArray<Face> faces = detector.detect(frame);

        Bitmap resultBitmap = image;

        if (faces.size() != 0) {
            Log.d("Faces", "faces detected count = " + faces.size());

            for (int i = 0; i < faces.size(); i++) {
                Bitmap emojiBitmap = null;
                Resources res = context.getResources();

                Face face = faces.valueAt(i);

                switch (whichEmoji(face)) {
                    case SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.smile);
                        break;
                    case FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.closed_frown);
                        break;
                    case NEUTRAL:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.neutral);
                        break;
                    case EXPRESSIONLESS:
                        emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.expressionless);
                        break;

                }

                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap, face);
            }

        } else {
            Toast.makeText(context, "no faces was detected", Toast.LENGTH_SHORT).show();
        }

        detector.release();

        return resultBitmap;
    }

    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }

    private static Emoji whichEmoji(Face face) {
        Emoji emoji;

        Log.d("Faces", "smiling = " + face.getIsSmilingProbability());
        boolean leftEyeOpen = EYE_OPEN_THRESHOLD <= face.getIsLeftEyeOpenProbability();
        boolean rightEyeOpen = EYE_OPEN_THRESHOLD <= face.getIsRightEyeOpenProbability();
        boolean smiling = SMILING_THRESHOLD <= face.getIsSmilingProbability();
        boolean neutral = NEUTRAL_THRESHOLD <= face.getIsSmilingProbability();

        if (smiling) {
            if (!leftEyeOpen && rightEyeOpen) {
                emoji = Emoji.LEFT_WINK;
            } else if (!rightEyeOpen && leftEyeOpen) {
                emoji = Emoji.RIGHT_WINK;
            } else if (rightEyeOpen) {
                emoji = Emoji.SMILE;
            } else {
                emoji = Emoji.CLOSED_EYE_SMILE;
            }
        } else if (neutral) {
            if (leftEyeOpen) {
                emoji = Emoji.NEUTRAL;
            } else {
                emoji = Emoji.EXPRESSIONLESS;
            }
        } else {
            if (!leftEyeOpen && rightEyeOpen) {
                emoji = Emoji.LEFT_WINK_FROWN;
            } else if (!rightEyeOpen && leftEyeOpen) {
                emoji = Emoji.RIGHT_WINK_FROWN;
            } else if (rightEyeOpen) {
                emoji = Emoji.FROWN;
            } else {
                emoji = Emoji.CLOSED_EYE_FROWN;
            }
        }

        Log.d("Faces", emoji.name());

        return emoji;
    }

    private enum Emoji {
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN,
        NEUTRAL,
        EXPRESSIONLESS
    }

}
