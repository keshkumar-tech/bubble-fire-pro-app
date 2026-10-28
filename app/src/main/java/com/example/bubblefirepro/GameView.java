package com.example.bubblefirepro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class GameView extends View {

    public interface GameListener {
        void onScoreChanged(int score);
        void onLevelChanged(int level);
        void onGameOver();
        void onMovesChanged(int moves);
        void onLevelWin(int level, int score);
        void onPerformanceAchieved(String performance);
        void playSound(String type);
    }

    private GameListener listener;
    private Paint paint, aimPaint, textPaint, dividerPaint, glowPaint;
    private int screenWidth, screenHeight;
    private float shooterX, shooterY;
    
    private Bubble currentBubble;
    private Bubble nextBubble;
    private ArrayList<Bubble> staticBubbles = new ArrayList<>();
    private ArrayList<PoppingBubble> poppingBubbles = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    
    private int[] colors = {
        Color.parseColor("#FF3D00"), // Red
        Color.parseColor("#2979FF"), // Blue
        Color.parseColor("#00E676"), // Green
        Color.parseColor("#FFEA00"), // Yellow
        Color.parseColor("#D500F9"), // Purple
        Color.parseColor("#00E5FF")  // Cyan
    };
    
    private Random random = new Random();
    private boolean isFiring = false;
    private boolean isAiming = false;
    private float aimX, aimY;
    
    private static final float BUBBLE_RADIUS = 42f;
    private float currentSpeed = 120f;

    private int score = 0;
    private int level = 1;
    private int moves = 20;
    private String activePowerUp = "NONE";

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        aimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        aimPaint.setColor(Color.WHITE);
        aimPaint.setStyle(Paint.Style.STROKE);
        aimPaint.setStrokeWidth(6);
        aimPaint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(Color.parseColor("#44000000"));
        dividerPaint.setStrokeWidth(4);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    public void initLevel(int level) {
        this.level = level;
        this.moves = 20 + (level / 2); 
        this.currentSpeed = 120f + (level * 2); 
        staticBubbles.clear();
        messages.clear();
        isFiring = false;
        activePowerUp = "NONE";
        
        if (screenWidth == 0) return;

        float spacingX = BUBBLE_RADIUS * 2.0f;
        float spacingY = BUBBLE_RADIUS * 1.73f;
        int cols = (int) (screenWidth / spacingX);
        int rows = 18; 
        float startY = 120f;
        float totalWidth = cols * spacingX;
        float horizontalPadding = (screenWidth - totalWidth) / 2f;

        int patternType = level % 4; 

        for (int row = 0; row < rows; row++) {
            int rowColor = colors[random.nextInt(colors.length)];
            for (int col = 0; col < cols; col++) {
                float offsetX = (row % 2 == 0) ? 0 : BUBBLE_RADIUS;
                float x = col * spacingX + BUBBLE_RADIUS + horizontalPadding + offsetX;
                float y = row * spacingY + BUBBLE_RADIUS + startY;

                boolean shouldAdd = false;
                int bubbleColor = rowColor;

                if (patternType == 0) { // Solid Rows Full Screen
                    if (row < 13) shouldAdd = true;
                } else if (patternType == 1) { // Massive Diamond
                    if (Math.abs(col - (cols/2f)) + Math.abs(row - 6) < 7) shouldAdd = true;
                } else if (patternType == 2) { // Full V-Shape
                    if (row < 14 && (col >= cols/2f - row/2f && col <= cols/2f + row/2f)) shouldAdd = true;
                } else { // Heart/Shield Dense
                    float cx = cols / 2f;
                    if (row < 14 && Math.abs(col - cx) < (8 - Math.abs(row - 7)/1.2f)) shouldAdd = true;
                }

                if (shouldAdd && x < screenWidth - (BUBBLE_RADIUS/2) && x > (BUBBLE_RADIUS/2) && y < shooterY - 220) {
                    staticBubbles.add(new Bubble(x, y, bubbleColor));
                }
            }
        }
        
        if (listener != null) {
            listener.onLevelChanged(level);
            listener.onMovesChanged(moves);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        shooterX = screenWidth / 2f;
        shooterY = screenHeight - 450;
        initLevel(level);
        prepareBubbles();
    }

    private void prepareBubbles() {
        if (currentBubble == null) {
            currentBubble = new Bubble(shooterX, shooterY, colors[random.nextInt(colors.length)]);
        }
        int nextC;
        do { nextC = colors[random.nextInt(colors.length)]; } while (nextC == currentBubble.color);
        nextBubble = new Bubble(shooterX + 160, shooterY + 50, nextC);
        nextBubble.radius = BUBBLE_RADIUS * 0.8f;
    }

    public void swapBubbles() {
        if (isFiring) return;
        int t = currentBubble.color;
        currentBubble.color = nextBubble.color;
        nextBubble.color = t;
        if (listener != null) listener.playSound("SWAP");
        invalidate();
    }

    public void activatePowerUp(String type) {
        if (isFiring) return;
        activePowerUp = type;
        currentBubble.radius = BUBBLE_RADIUS * 1.6f; 
        if (type.equals("BOMB")) currentBubble.color = Color.BLACK;
        else if (type.equals("FIRE")) currentBubble.color = Color.parseColor("#FF4500");
        else if (type.equals("NUCLEAR")) currentBubble.color = Color.parseColor("#4CAF50");
        else if (type.equals("ELECTRIC")) currentBubble.color = Color.parseColor("#FFEB3B");
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#A6B1FF")); 
        canvas.drawLine(0, 80, screenWidth, 80, dividerPaint);

        update();

        if (isAiming && !isFiring) canvas.drawLine(shooterX, shooterY, aimX, aimY, aimPaint);

        for (Bubble b : staticBubbles) drawGlossyBubble(canvas, b);
        
        Iterator<PoppingBubble> pit = poppingBubbles.iterator();
        while (pit.hasNext()) {
            PoppingBubble p = pit.next();
            p.radius += 10; p.alpha -= 20;
            if (p.alpha <= 0) pit.remove();
            else {
                paint.setColor(p.color); paint.setAlpha(p.alpha);
                canvas.drawCircle(p.x, p.y, p.radius, paint);
                paint.setAlpha(255);
            }
        }

        Iterator<Message> mit = messages.iterator();
        while (mit.hasNext()) {
            Message m = mit.next();
            m.y -= 3; m.alpha -= 5;
            if (m.alpha <= 0) mit.remove();
            else {
                textPaint.setColor(m.color); textPaint.setAlpha(m.alpha); textPaint.setTextSize(90);
                canvas.drawText(m.text, screenWidth/2, m.y, textPaint);
            }
        }

        if (currentBubble != null) {
            if (!activePowerUp.equals("NONE")) {
                glowPaint.setShader(new RadialGradient(currentBubble.x, currentBubble.y, currentBubble.radius * 1.3f, 
                        new int[]{Color.YELLOW, Color.RED, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP));
                canvas.drawCircle(currentBubble.x, currentBubble.y, currentBubble.radius * 1.3f, glowPaint);
            }
            drawGlossyBubble(canvas, currentBubble);
        }
        if (nextBubble != null) drawGlossyBubble(canvas, nextBubble);

        invalidate();
    }

    private void drawGlossyBubble(Canvas canvas, Bubble b) {
        int core = b.color;
        if (b.color == Color.BLACK) core = Color.DKGRAY;
        RadialGradient g = new RadialGradient(b.x - b.radius/3, b.y - b.radius/3, b.radius * 1.5f,
                new int[]{Color.WHITE, core, Color.BLACK}, new float[]{0f, 0.7f, 1f}, Shader.TileMode.CLAMP);
        paint.setShader(g);
        canvas.drawCircle(b.x, b.y, b.radius, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE); paint.setAlpha(180);
        canvas.drawCircle(b.x - b.radius/2.5f, b.y - b.radius/2.5f, b.radius/4, paint);
        paint.setAlpha(255);
    }

    private void update() {
        if (isFiring && currentBubble != null) {
            currentBubble.x += currentBubble.vx; currentBubble.y += currentBubble.vy;
            if (currentBubble.x - currentBubble.radius < 0 || currentBubble.x + currentBubble.radius > screenWidth) currentBubble.vx = -currentBubble.vx;
            if (currentBubble.y - currentBubble.radius < 80) { stickBubble(); return; }
            for (Bubble b : staticBubbles) {
                float d = (float) Math.sqrt(Math.pow(currentBubble.x - b.x, 2) + Math.pow(currentBubble.y - b.y, 2));
                if (d < (currentBubble.radius + b.radius) * 0.9f) { stickBubble(); break; }
            }
        }
    }

    private void stickBubble() {
        isFiring = false;
        Bubble n = new Bubble(currentBubble.x, currentBubble.y, currentBubble.color);
        n.radius = BUBBLE_RADIUS; 
        staticBubbles.add(n);
        
        if (listener != null) listener.playSound("HIT");

        int initialSize = staticBubbles.size();
        if (!activePowerUp.equals("NONE")) {
            applyPowerUp(n);
            activePowerUp = "NONE";
        } else {
            checkMatches(n);
        }
        
        dropDisconnected();
        int popped = initialSize - staticBubbles.size();
        
        if (popped >= 10) {
            messages.add(new Message("EXCELLENT!!", Color.RED, screenHeight/2));
            if (listener != null) listener.onPerformanceAchieved("EXCELLENT");
        } else if (popped >= 6) {
            messages.add(new Message("GREAT!", Color.YELLOW, screenHeight/2));
            if (listener != null) listener.onPerformanceAchieved("GREAT");
        } else if (popped >= 3) {
            messages.add(new Message("GOOD", Color.WHITE, screenHeight/2));
        }
        
        if (popped >= 3 && listener != null) listener.playSound("POP");

        moves--;
        if (listener != null) {
            listener.onMovesChanged(moves);
            if (staticBubbles.isEmpty()) listener.onLevelWin(level, score);
            else if (moves <= 0) listener.onGameOver();
        }

        currentBubble.x = shooterX; currentBubble.y = shooterY;
        currentBubble.radius = BUBBLE_RADIUS; 
        currentBubble.color = nextBubble.color;
        prepareBubbles();
    }

    private void applyPowerUp(Bubble center) {
        ArrayList<Bubble> toRem = new ArrayList<>();
        float range = BUBBLE_RADIUS * 5;
        if (activePowerUp.equals("NUCLEAR")) range = BUBBLE_RADIUS * 9;
        
        for (Bubble b : staticBubbles) {
            float d = (float) Math.sqrt(Math.pow(b.x - center.x, 2) + Math.pow(b.y - center.y, 2));
            if (d < range) toRem.add(b);
        }
        for (Bubble b : toRem) poppingBubbles.add(new PoppingBubble(b.x, b.y, b.color));
        staticBubbles.removeAll(toRem);
        score += toRem.size() * 30;
        if (listener != null) {
            listener.onScoreChanged(score);
            listener.playSound("POP");
        }
    }

    private void checkMatches(Bubble root) {
        ArrayList<Bubble> matches = new ArrayList<>();
        ArrayList<Bubble> queue = new ArrayList<>();
        queue.add(root); matches.add(root);
        int head = 0;
        while(head < queue.size()){
            Bubble curr = queue.get(head++);
            for(Bubble b : staticBubbles){
                if(!matches.contains(b) && b.color == curr.color){
                    if(Math.sqrt(Math.pow(curr.x-b.x,2)+Math.pow(curr.y-b.y,2)) < BUBBLE_RADIUS*2.3f){
                        matches.add(b); queue.add(b);
                    }
                }
            }
        }
        if(matches.size() >= 3){
            for(Bubble m : matches) poppingBubbles.add(new PoppingBubble(m.x, m.y, m.color));
            staticBubbles.removeAll(matches);
            score += matches.size() * 25;
            if (listener != null) listener.onScoreChanged(score);
        }
    }

    private void dropDisconnected() {
        if(staticBubbles.isEmpty()) return;
        Set<Bubble> connected = new HashSet<>();
        ArrayList<Bubble> queue = new ArrayList<>();
        for(Bubble b : staticBubbles) if(b.y < BUBBLE_RADIUS * 2 + 120) { connected.add(b); queue.add(b); }
        int h = 0;
        while(h < queue.size()){
            Bubble c = queue.get(h++);
            for(Bubble b : staticBubbles){
                if(!connected.contains(b) && Math.sqrt(Math.pow(c.x-b.x,2)+Math.pow(c.y-b.y,2)) < BUBBLE_RADIUS*2.3f){
                    connected.add(b); queue.add(b);
                }
            }
        }
        ArrayList<Bubble> disc = new ArrayList<>();
        for(Bubble b : staticBubbles) if(!connected.contains(b)) { disc.add(b); poppingBubbles.add(new PoppingBubble(b.x,b.y,b.color)); }
        staticBubbles.removeAll(disc);
        score += disc.size() * 15;
        if (listener != null) listener.onScoreChanged(score);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFiring) return true;
        float tx = event.getX(), ty = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (ty < shooterY) { isAiming = true; aimX = tx; aimY = ty; }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isAiming = false;
            if (ty < shooterY - 50) {
                float dx = tx - shooterX, dy = ty - shooterY, d = (float) Math.sqrt(dx*dx+dy*dy);
                currentBubble.vx = (dx/d)*currentSpeed; currentBubble.vy = (dy/d)*currentSpeed; isFiring = true;
                if (listener != null) listener.playSound("FIRE");
            }
        }
        performClick(); return true;
    }

    @Override public boolean performClick() { return super.performClick(); }

    class Bubble {
        float x, y, radius; int color; float vx, vy;
        Bubble(float x, float y, int color) { this.x=x; this.y=y; this.color=color; this.radius=BUBBLE_RADIUS; }
    }
    class PoppingBubble {
        float x, y, radius; int color; int alpha = 255;
        PoppingBubble(float x, float y, int color) { this.x=x; this.y=y; this.color=color; this.radius=BUBBLE_RADIUS; }
    }
    class Message {
        String text; int color; float y; int alpha = 255;
        Message(String t, int c, float y) { this.text=t; this.color=c; this.y=y; }
    }
}
