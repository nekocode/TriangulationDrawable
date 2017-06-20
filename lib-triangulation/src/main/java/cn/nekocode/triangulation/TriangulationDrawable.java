/*
 * Copyright 2017. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.triangulation;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class TriangulationDrawable extends Drawable implements Animatable, Runnable {
    private static final float FRAME_DURATION = 1000f / 60f;
    private static final float LOOP_DURANTION = 10000f;
    private boolean isRunning = false;
    private long startTicks = 0;
    private float loopPercent = 0f;

    private final Paint paint = new Paint();
    private final Path path = new Path();
    private int width = -1, height = -1;
    private int backgroundColor;
    private int alpha = 0xFF;
    private int numPointsX;
    private int numPointsY;
    private int unitWidth;
    private int unitHeight;
    private Point[] points;
    private ArrayList<Polygon> polygons;

    public TriangulationDrawable() {
        this(0xffef0e39);
    }

    public TriangulationDrawable(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    private void drawPolygons(@NonNull Canvas canvas) {
        if (isRunning()) {
            loopPercent = (AnimationUtils.currentAnimationTimeMillis() - startTicks) / LOOP_DURANTION;
            while (loopPercent > 1) {
                loopPercent -= 1f;
                startTicks += LOOP_DURANTION;

                for(Polygon polygon : polygons) {
                    polygon.points[0] = points[polygon.point1].x;
                    polygon.points[1] = points[polygon.point1].y;
                    polygon.points[2] = points[polygon.point2].x;
                    polygon.points[3] = points[polygon.point2].y;
                    polygon.points[4] = points[polygon.point3].x;
                    polygon.points[5] = points[polygon.point3].y;
                }
                randomize();
            }
        }

        for(Polygon polygon : polygons) {
            final float x1 = polygon.points[0] * (1f - loopPercent) + points[polygon.point1].x * (loopPercent);
            final float y1 = polygon.points[1] * (1f - loopPercent) + points[polygon.point1].y * (loopPercent);

            final float x2 = polygon.points[2] * (1f - loopPercent) + points[polygon.point2].x * (loopPercent);
            final float y2 = polygon.points[3] * (1f - loopPercent) + points[polygon.point2].y * (loopPercent);

            final float x3 = polygon.points[4] * (1f - loopPercent) + points[polygon.point3].x * (loopPercent);
            final float y3 = polygon.points[5] * (1f - loopPercent) + points[polygon.point3].y * (loopPercent);

            path.reset();
            path.moveTo(x1, y1);
            path.lineTo(x2, y2);
            path.lineTo(x3, y3);
            path.close();
            paint.setColor(0xFF000000);
            paint.setAlpha((int) (polygon.alpha * alpha));
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawColor(((backgroundColor & 0x00FFFFFF) + (alpha << 24)));

        if (canvas.getWidth() == width && canvas.getHeight() == height) {
            drawPolygons(canvas);
            return;
        }

        /*
          When canvas resized
         */
        width = canvas.getWidth();
        height = canvas.getHeight();

        final int unitSize = (width + height) / 20;
        numPointsX = (int) (Math.ceil(width / unitSize) + 1);
        numPointsY = (int) (Math.ceil(height / unitSize) + 1);
        unitWidth = (int) Math.ceil(width / (numPointsX - 1));
        unitHeight = (int) Math.ceil(height / (numPointsY - 1));

        points = new Point[numPointsY * numPointsX];
        for (int y = 0; y < numPointsY; y++) {
            for (int x = 0; x < numPointsX; x++) {
                points[x + y * numPointsX] =
                        new Point(unitWidth * x, unitHeight * y, unitWidth * x, unitHeight * y);
            }
        }

        randomize();

        polygons = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            if (points[i].originX != unitWidth * (numPointsX - 1) &&
                    points[i].originY != unitHeight * (numPointsY - 1)) {
                final float topLeftX = points[i].x;
                final float topLeftY = points[i].y;
                final float topRightX = points[i + 1].x;
                final float topRightY = points[i + 1].y;
                final float bottomLeftX = points[i + numPointsX].x;
                final float bottomLeftY = points[i + numPointsX].y;
                final float bottomRightX = points[i + numPointsX + 1].x;
                final float bottomRightY = points[i + numPointsX + 1].y;

                final int rando = (int) Math.floor(Math.random() * 2);

                for (int n = 0; n < 2; n++) {
                    final Polygon polygon = new Polygon();

                    if (rando == 0) {
                        if (n == 0) {
                            polygon.point1 = i;
                            polygon.point2 = i + numPointsX;
                            polygon.point3 = i + numPointsX + 1;
                            polygon.points = new float[] {
                                    topLeftX, topLeftY,
                                    bottomLeftX, bottomLeftY,
                                    bottomRightX, bottomRightY
                            };

                        } else if (n == 1) {
                            polygon.point1 = i;
                            polygon.point2 = i + 1;
                            polygon.point3 = i + numPointsX + 1;
                            polygon.points = new float[] {
                                    topLeftX, topLeftY,
                                    topRightX, topRightY,
                                    bottomRightX, bottomRightY
                            };
                        }

                    } else if (rando == 1) {
                        if (n == 0) {
                            polygon.point1 = i;
                            polygon.point2 = i + numPointsX;
                            polygon.point3 = i + 1;
                            polygon.points = new float[] {
                                    topLeftX, topLeftY,
                                    bottomLeftX, bottomLeftY,
                                    topRightX, topRightY
                            };

                        } else if (n == 1) {
                            polygon.point1 = i + numPointsX;
                            polygon.point2 = i + 1;
                            polygon.point3 = i + numPointsX + 1;
                            polygon.points = new float[] {
                                    bottomLeftX, bottomLeftY,
                                    topRightX, topRightY,
                                    bottomRightX, bottomRightY
                            };
                        }
                    }
                    polygon.alpha = (float) (Math.random() / 3);

                    polygons.add(polygon);
                }
            }
        }

        randomize();
        drawPolygons(canvas);
    }

    private void randomize() {
        for (int i = 0; i < points.length; i++) {
            if (points[i].originX != 0 && points[i].originX != unitWidth * (numPointsX - 1)) {
                points[i].x = (float) (points[i].originX + Math.random() * unitWidth - unitWidth / 2);
            }
            if (points[i].originY != 0 && points[i].originY != unitHeight * (numPointsY - 1)) {
                points[i].y = (float) (points[i].originY + Math.random() * unitHeight - unitHeight / 2);
            }
        }
    }

    @Override
    public void start() {
        if (!isRunning()) {
            isRunning = true;
            startTicks = AnimationUtils.currentAnimationTimeMillis();
            run();
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            unscheduleSelf(this);
            isRunning = false;
        }
    }

    @Override
    public void run() {
        invalidateSelf();
        scheduleSelf(this, (long) (AnimationUtils.currentAnimationTimeMillis() + FRAME_DURATION));
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private static class Point {
        private float x;
        private float y;
        private float originX;
        private float originY;

        Point(float x, float y, float originX, float originY) {
            this.x = x;
            this.y = y;
            this.originX = originX;
            this.originY = originY;
        }
    }

    private static class Polygon {
        private int point1;
        private int point2;
        private int point3;
        private float[] points;
        private float alpha;
    }
}
