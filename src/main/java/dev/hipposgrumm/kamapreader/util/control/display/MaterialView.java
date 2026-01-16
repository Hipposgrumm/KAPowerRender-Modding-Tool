package dev.hipposgrumm.kamapreader.util.control.display;

import dev.hipposgrumm.kamapreader.util.types.Material;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

// https://github.com/jvm-graphics-labs/hello-triangle/blob/master/src/main/java/gl3/HelloTriangleSimple.java
public class MaterialView {
    public static Node create(Material material, double width, double height) {
        Node changableView;
        try {
            Group root = new Group();

            Sphere sphere = new Sphere(50);
            sphere.getTransforms().add(new Rotate(20, Rotate.X_AXIS));

            PhongMaterial pongMaterial = new PhongMaterial();
            updateMaterial(material, sphere, pongMaterial);
            sphere.setMaterial(pongMaterial);

            Camera camera = new PerspectiveCamera();
            camera.setTranslateX(-width/2);
            camera.setTranslateY(-height/2);

            PointLight light = new PointLight(Color.WHITE);
            light.setTranslateX(-500);
            light.setTranslateY(-500);
            light.setTranslateZ(-500);

            AmbientLight ambientLight = new AmbientLight(Color.GRAY);
            sphere.setRotationAxis(new Point3D(2, 1, 0).normalize());
            root.getChildren().addAll(ambientLight, light, sphere);

            SubScene subScene = new SubScene(root, width, height, true, SceneAntialiasing.DISABLED);
            subScene.setFill(Color.TEAL);
            subScene.setCamera(camera);

            new AnimationTimer() {
                private long last = 0;

                @Override
                public void handle(long now) {
                    if (last == 0) last = now;
                    if (subScene.getScene() == null) {
                        stop();
                        return;
                    }

                    updateMaterial(material, sphere, pongMaterial);
                    sphere.getTransforms().add(new Rotate((now-last)/10000000d, Rotate.Y_AXIS));
                    last = now;
                }
            }.start();

            changableView = subScene;
        } catch (Exception e) {
            e.printStackTrace();
            changableView = new Label("Preview could not be loaded.");
        }
        return changableView;
    }

    private static void updateMaterial(Material material, Sphere sphere, PhongMaterial pongMaterial) {
        pongMaterial.setDiffuseColor(material.color.toJavaFXColor());
        if (material.textures[0] != null) // TODO: Multi-layer?
            pongMaterial.setDiffuseMap(material.textures[0].getViewable().getJavaFXImage());
        pongMaterial.setSpecularColor(material.specular.toJavaFXColor());
        pongMaterial.setSpecularPower(material.specular_power);
        sphere.setCullFace(material.doublesided ? CullFace.NONE : CullFace.BACK);
    }
}
