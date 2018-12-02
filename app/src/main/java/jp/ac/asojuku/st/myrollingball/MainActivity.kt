package jp.ac.asojuku.st.myrollingball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener,SurfaceHolder.Callback{

    //プロパティ
    private var surfaceWidth:Int = 0//サーフェスの幅
    private var surfaceHeight:Int = 0//サーフェスの高さ

    private val radius = 50.0f //ボールの半径
    private val coef = 1000.0f //ボールの移動量を計算するための係数

    private var ballX:Float = 0f //ボールの現在のX座標
    private var ballY:Float = 0f //ボールの現在のY座標
    private var vx:Float = 0f //ボールのX方向の加速度
    private var vy:Float = 0f //ボールのY方向の加速度
    private var time:Long = 0L //前回の取得時間


    //ゴール/プロパティ
    private var gollA:Float = 1f
    private var gollB:Float = 1f
    private var gollC:Float = 2f+150f
    private var gollD:Float = 2f+100f

    //障害物1
    private var blockA:Float = 500f
    private var blockB:Float = 600f
    private var blockC:Float = 900f
    private var blockD:Float = 700f

    //障害物2
    private var block2A:Float = 500f
    private var block2B:Float = 600f
    private var block2C:Float = 700f
    private var block2D:Float = 700f

    //障害物3
    private var block3A:Float = 3f
    private var block3B:Float = 5f
    private var block3C:Float = 2f+70f
    private var block3D:Float = 2f+46f


    //誕生時のライフサイクルイベント
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var holder = SurfaceView.holder //サーフェスホルダ―を取得
        //サーフェスホルダ―にコールバックに自クラスを追加
        holder.addCallback(this)
        //画面の縦横指定をアプリから指定して
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    //画面表示・再表示のライフサイクルイベント
    override fun onResume() {
        super.onResume()

        //リセット押されたとき
        reset.setOnClickListener {
             ballX = 0f
             ballY = 0f
             vx = 0f
             vy = 0f
             time = 0L
            val messageView: TextView = findViewById(R.id.comment)
            messageView.text = "いけるぞー"
        }
        //自クラスのonResume()処理
        //センサーマネージャをOSから取得
//        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        //加速度センサー(Accelerometer)を指定してセンサーマネージャからセンサーを取得
//        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        //リスナー登録して加速度センサーの監視を開始
//        sensorManager.registerListener(
//                this,//イベントリスナー機能を持つインスタンス(自クラスのインスタンス)
//                accSensor,//監視するセンサー(加速度センサー)
//                SensorManager.SENSOR_DELAY_GAME//センサーの更新頻度
//        )
    }
    //画面が非表示の時のライフサイクルイベント
    override fun onPause(){
        super.onPause()
        //センサーマネージャ取得
//        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE)
//                as SensorManager
//        //センサーマネージャに登録したリスナーを解除(自分自信を解除)
//        sensorManager.unregisterListener(this)
    }

    //精度が変わった時のイベントコールバック
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    //センサーの値が変わった時のイベントコールバック
    override fun onSensorChanged(event: SensorEvent?) {
        //イベントが何もなかったらそのままリターン
        if (event == null) {
            return
        }
//        //センサーの値が変わったらログ出力
//        //加速度センサーか判定
//        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
//            //ログ出力用文字列を組み立て
//            val str = "x = ${event.values[0].toString()}"+
//                    ", y = ${event.values[1].toString()}"+
//                    ", z = ${event.values[2].toString()}"
//            //デバッグログに出力
//            //Log.d("加速度センサー",str)
//
//        }
        //ボールの描画の計算処理
        if (time == 0L) {
            time = System.currentTimeMillis();
        }//最初のタイミングでは現在時刻を保存
        //イベントのセンサー種別の情報がアクセラメーター（加速度センサー）の時だけ以下の処理を実行
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            //センサーのx(左右),y(縦)値を取得
            val x = event.values[0] * -1;
            val y = event.values[1];

            //経過時間を計算
            //(今の時間 - 前の時間 = 経過時間)
            var t = (System.currentTimeMillis() - time).toFloat();
            //今の時間を「前の時間」として保存
            time = System.currentTimeMillis();
            t /= 1000.0f;

            //移動距離を計算(ボールをどのくらい動かすか)
            val dx = (vx * t) + (x * t * t) / 2.0f; //xの移動距離(メートル)
            val dy = (vy * t) + (y * t * t) / 2.0f; //yの移動距離(メートル)
            //メートルのピクセルのcmに補正してボールのX座標に足しこむ=新しいボールのX座標
            ballX += (dx * coef)
            //メートルのピクセルのcmに補正してボールのY座標に足しこむ=新しいボールのY座標
            ballY += (dy * coef)
            //今の加速度を更新
            vx += (x * t)
            vy += (y * t)

            //画面の端に来たら跳ね返る処理
            if ((ballX - radius) < 0 && vx < 0) {
                //左にぶつかった時
                vx = -vx / 1.5f;
                ballX = radius;
            } else if ((ballX + radius) > surfaceWidth && vx > 0) {
                //右にぶつかった時
                vx = -vx / 1.5f;
                ballX = (surfaceWidth - radius);
            }
            //上下について
            if ((ballY - radius < 0) && vy < 0) {
                //下にぶつかった時
                vy = -vy / 1.5f;
                ballY = radius;
                //上にぶつかった時
            } else if ((ballY + radius) > surfaceHeight && vy > 0) {
                vy = -vy / 1.5f;
                ballY = surfaceHeight - radius;
            }


            //ゴールに当たった処理
            //上下
            if((ballX-radius)<gollC//右側
                    &&(ballX+radius)>gollA//左側
                    &&(ballY-radius)<gollD//下側
                    &&(ballY+radius)>gollB//上側
            ) {
                val messageView: TextView = findViewById(R.id.comment)
                messageView.text = "げーむくりあ"
                vx = vx/1.6f;
                vy = vy/1.6f;
            }

            //障害物あたり判定
            if((ballX-radius)<blockC//右側
                    &&(ballX+radius)>blockA//左側
                    &&(ballY-radius)<blockD//下側
                    &&(ballY+radius)>blockB//上側
            ) {
                val messageView: TextView = findViewById(R.id.comment)
                messageView.text = "GAME OVER"
                vx = vx/1.6f;
                vy = vy/1.6f;
            }

            //reset.


            this.drawCanvas()
        }
    }


    //サーフェスが更新された時のイベント
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        //サーフェスの幅と高さをプロパティに保存
        surfaceWidth = width
        surfaceHeight = height
        //ボールの初期位置を保存しておく

        ballX = (width/2).toFloat()
        ballY = (height/2).toFloat()
    }

    //サーフェスが破棄された時のイベント
    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        //加速度センサーの登録を解除する流れ
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //センサーマネージャを通じてOSからリスナー登録(自分自身)を登録解除
        sensorManager.unregisterListener(this)
    }

    //サーフェスが作成されたときのイベント
    override fun surfaceCreated(holder: SurfaceHolder?) {
        //加速度センサーのリスナーを登録する流れ
        //センサーマネージャーを取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //センサーマネージャーから加速度センサーを取得
        val accSensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //加速度センサーのリスナーをOSに取得
        sensorManager.registerListener(
                this,//リスナー(自クラス)
                accSensor,//加速度センサー
                SensorManager.SENSOR_DELAY_GAME //センシングの頻度
        )
    }
    //サーフェスのキャンバスに描画するメソッド
    private fun drawCanvas(){
        //キャンバスをロックして取得
        val canvas = SurfaceView.holder.lockCanvas()
        //キャンバスの背景色を設定
        canvas.drawColor(Color.BLACK)

        canvas.drawRect(blockA,blockB,blockC,blockD,
                Paint().apply {
                    color = Color.RED
        }
        )

//        canvas.drawRect(block2A/4f,block2B/4f,block2C/4f+100f,block2D/4f+80f,
//                Paint().apply {
//                    color = Color.GRAY
//                }
//        )
//            canvas.drawRect(surfaceWidth/4f,surfaceHeight/4f,surfaceWidth/4f+5f,surfaceHeight/4f+80f,
//                Paint().apply {
//                    color = Color.GRAY
//                }
//        )
//        canvas.drawRect(block2A,block2B,block2C,block2D,
//                Paint().apply {
//                    color = Color.WHITE
//                }
//        )
        //ゴール
        canvas.drawRect(gollA,gollB,gollC,gollD,
                Paint().apply {
                    color = Color.GREEN
                }
        )
        //キャンバスに円を描いてボールにする
        canvas.drawCircle(ballX,//ボール中心のX座標
                ballY,//ボール中心YX座標

                radius,//半径
                Paint().apply{
                    color = Color.YELLOW }//ペイントブラシのインスタンス
        )

        //キャンバスをロック
        SurfaceView.holder.unlockCanvasAndPost(canvas);


    }

}
