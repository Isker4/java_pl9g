import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    class GameView extends SurfaceView implements Runnable {
        private Thread gameThread;
        private volatile boolean playing;
        private SurfaceHolder holder;
        private Paint paint;
        private Random random;
        
        // Персонажи
        private List<FamilyMember> family;
        private List<Gift> gifts;
        
        // Размеры экрана
        private int screenWidth, screenHeight;

        public GameView(Context context) {
            super(context);
            holder = getHolder();
            paint = new Paint();
            random = new Random();
            
            // Настройка шрифта для эмодзи
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextSize(100);
            
            initFamily();
        }

        private void initFamily() {
            family = new ArrayList<>();
            family.add(new FamilyMember("👩", 2.5f, new String[]{"💐", "👜", "☕"})); // Мама
            family.add(new FamilyMember("👨", 2.0f, new String[]{"🎮", "🎸", "🍔"})); // Папа
            family.add(new FamilyMember("👧", 3.0f, new String[]{"🎀", "📚", "🎨"})); // Дочь
            family.add(new FamilyMember("👦", 3.5f, new String[]{"🚗", "⚽", "🍭"})); // Сын 7 лет
            family.add(new FamilyMember("🧒", 1.5f, new String[]{"🍼", "🧸", "🐻"})); // Сын 2 лет
            
            // Начальные позиции
            int startX = 100;
            for(FamilyMember member : family) {
                member.x = startX;
                member.y = screenHeight - 300;
                startX += 200;
            }
        }

        @Override
        public void run() {
            while (playing) {
                if(holder.getSurface().isValid()) {
                    update();
                    draw();
                }
                
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void update() {
            // Обновление позиций персонажей
            for(FamilyMember member : family) {
                member.x += member.speed;
                if(member.x > screenWidth - 100 || member.x < 0) {
                    member.speed *= -1;
                }
            }
            
            // Обновление позиций подарков
            for(Gift gift : gifts) {
                gift.y += 10;
            }
        }

        private void draw() {
            Canvas canvas = holder.lockCanvas();
            if(canvas == null) return;
            
            // Фон
            canvas.drawColor(Color.parseColor("#87CEEB")); // Небо
            
            // Песок
            paint.setColor(Color.parseColor("#EED6AF"));
            canvas.drawRect(0, screenHeight - 200, screenWidth, screenHeight, paint);
            
            // Персонажи
            paint.setColor(Color.WHITE);
            for(FamilyMember member : family) {
                canvas.drawText(member.emoji, member.x, member.y, paint);
            }
            
            // Подарки
            for(Gift gift : gifts) {
                canvas.drawText(gift.emoji, gift.x, gift.y, paint);
            }
            
            holder.unlockCanvasAndPost(canvas);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                // Добавление нового подарка
                FamilyMember member = family.get(random.nextInt(family.size()));
                gifts.add(new Gift(
                    member.x + random.nextInt(100) - 50,
                    -100,
                    member.gifts[random.nextInt(member.gifts.length)]
                ));
            }
            return true;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            screenWidth = w;
            screenHeight = h;
            initFamily();
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    static class FamilyMember {
        String emoji;
        float x, y, speed;
        String[] gifts;

        public FamilyMember(String emoji, float speed, String[] gifts) {
            this.emoji = emoji;
            this.speed = speed;
            this.gifts = gifts;
        }
    }

    static class Gift {
        float x, y;
        String emoji;

        public Gift(float x, float y, String emoji) {
            this.x = x;
            this.y = y;
            this.emoji = emoji;
        }
    }
}
