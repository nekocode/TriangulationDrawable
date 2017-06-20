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

package cn.nekocode.triangulation.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.nekocode.triangulation.TriangulationDrawable;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class MainActivity extends AppCompatActivity {
    private TriangulationDrawable triangulationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        triangulationDrawable = new TriangulationDrawable();
        findViewById(android.R.id.content).setBackground(triangulationDrawable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        triangulationDrawable.start();
    }

    @Override
    protected void onStop() {
        triangulationDrawable.stop();
        super.onStop();
    }
}
