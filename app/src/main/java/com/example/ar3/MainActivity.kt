package com.example.ar3

import android.app.Activity
import android.app.ActivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class MainActivity : AppCompatActivity() {
    private var nodePostRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }
        setContentView(R.layout.activity_main)
        objectSelect()
        val arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment?
        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, _: Plane?, _: MotionEvent? ->
            if (nodePostRenderable == null) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.parent = arFragment.arSceneView.scene
            val node = TransformableNode(arFragment.transformationSystem)
            node.scaleController.maxScale = 1f
            node.scaleController.minScale = 0.1f
            node.parent = anchorNode
            node.renderable = nodePostRenderable
            node.renderableInstance.animate(true).start()
            node.select()
        }
    }

    private fun objectSelect()
    {
        val objects = resources.getStringArray(R.array.Dinosaurs)
        val spinner = findViewById<Spinner>(R.id.spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(this, R.layout.spinner, objects)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    var res = R.raw.raptor
                    when(position) {
                        0 -> res = R.raw.raptor
                        1 -> res = R.raw.trex
                        2 -> res = R.raw.apatosaurus
                        3 -> res = R.raw.parasaurolophus
                        4 -> res = R.raw.stegosaurus
                        5 -> res = R.raw.triceratops
                    }
                    ModelRenderable.builder()
                        .setSource(this@MainActivity, res)
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept { renderable: ModelRenderable? ->
                            nodePostRenderable = renderable
                        }
                        .exceptionally {
                            val toast =
                                Toast.makeText(
                                    this@MainActivity,
                                    R.string.model_error,
                                    Toast.LENGTH_LONG
                                )
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                            null
                        }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    companion object {
        private const val MIN_OPENGL_VERSION = 3.0
    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        val openGlVersionString =
            (activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Toast.makeText(
                activity,
                R.string.version_error,
                Toast.LENGTH_LONG
            ).show()
            activity.finish()
            return false
        }
        return true
    }
}