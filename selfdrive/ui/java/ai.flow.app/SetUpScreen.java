package ai.flow.app;

import ai.flow.common.ParamsInterface;
import ai.flow.common.transformations.Camera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import ai.flow.app.CalibrationScreens.CalibrationInfo;

import static ai.flow.common.transformations.Camera.fcamIntrinsicParam;

public class SetUpScreen extends ScreenAdapter {

    FlowUI appContext;
    ParamsInterface params = ParamsInterface.getInstance();

    public SetUpScreen(FlowUI appContext) {
        this.appContext = appContext;
    }

    @Override
    public void show() {

        if (!params.existsAndCompare("HasAcceptedTerms", true)) {
            appContext.setScreen(new TermsScreen(appContext));
            return;
        }

        if (!params.exists("UserToken")) {
            appContext.setScreen(new RegisterScreen(appContext));
            return;
        }

        if (RequestSink.isConnectedToInternet()) {
            RequestSink.fetchUserInfo();
        }

        if (!RequestSink.isPlanStillValid()) {
            appContext.setScreen(new PlanInvalidScreen(appContext));
            return;
        }

        if (!RequestSink.isDeviceAllowed()) {
            appContext.setScreen(new DeviceNotAllowedScreen(appContext));
            return;
        }

        if (!params.existsAndCompare("CompletedTrainingVersion", true)){
            appContext.setScreen(new TrainingScreen(appContext));
            return;
        }

        // no need to calibrate fcam in WideCameraOnly mode.
        if (!params.exists(fcamIntrinsicParam) & !params.existsAndCompare("WideCameraOnly", true)){
           appContext.launcher.startSensorD();
           appContext.setScreen(new CalibrationInfo(appContext, Camera.CAMERA_TYPE_ROAD, false));
           return;
        }

        if (!params.exists("WideCameraMatrix") & appContext.isF3 & !params.existsAndCompare("WideCameraOnly", true)){
            appContext.launcher.startSensorD();
            appContext.setScreen(new CalibrationInfo(appContext, Camera.CAMERA_TYPE_WIDE, false));
            return;
        }

        appContext.launcher.startAllD();
        appContext.setScreen(new IntroScreen(appContext));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        params.dispose();
    }
}
