package com.vkyoungcn.learningtools.validatingEditor;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.vkyoungcn.learningtools.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.InputType.TYPE_CLASS_TEXT;

/**
 * Thanks to the original author.
 * This code is only being used under non_commercial situations.
 * @author Adrián García Lomas
 */
public class ValidatingEditor extends View {
    private static final String TAG = "ValidatingEditor";
    private static final int DEFAULT_LENGTH = 6;
    private static final String KEYCODE = "KEYCODE_";//按键事件返回的一定是KEYCODE_开头（已知字符）或数字1001（未知字符）
    private static final Pattern KEYCODE_PATTERN = Pattern.compile( KEYCODE + "(.)");//用括号来指示Match中的分组
    private Context mContext;
    private FixedStack<Character> characters;
    private String targetText = "";//用于比较的目标字串

    private BottomLineSection bottomLineSections[];
    private Paint bottomLinePaint;
    private Paint bottomLineNonCorrectPaint;
    private Paint textPaint;
    private Paint textNonCorrectPaint;
    private Paint backgroundPaint;

    private float bottomLineHorizontalMargin;
    private float bottomLineStrokeWidth;
    private float bottomLineHorizontalLength;
    private float padding;
    private float textSize;
    private float textMarginBottom;
    private float viewHeight;
    int lines = 1;//控件需要按几行显示，根据当前屏幕下控件最大允许宽度和控件字符数（需要的宽度）计算得到。
    private int sizeChangedHeight;//是控件onSizeChanged后获得的尺寸之高度，也是传给onDraw进行线段绘制的canvas-Y坐标(单行时)
    private int sizeChangedWidth;

    private int bottomLineSectionAmount = DEFAULT_LENGTH;
    private int bottomLineColor;
    private int bottomLineNonCorrectColor;
    private int textColor;
    private int textNonCorrectColor;
    private int backgroundColor;
    private int backgroundNotCorrectColor;
    private int mInputType;
    private int leastWrongPosition = 0;//从1起算，0是预置位。
    private int currentPosition = 0;//从1起算。

    private codeCorrectAndReadyListener listener;

    public ValidatingEditor(Context context) {
        super(context);
        mContext = context;
        init(null);
        this.listener = null;
    }

    public ValidatingEditor(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mContext = context;
        init(attributeset);
        this.listener = null;
    }


    public ValidatingEditor(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        mContext = context;
        init(attributeset);
        this.listener = null;
    }

    public void setCodeReadyListener(codeCorrectAndReadyListener listener) {
        this.listener = listener;
    }

    private void init(AttributeSet attributeset) {
        initDefaultAttributes();
        initCustomAttributes(attributeset);
        initPaint();
        initViewOptions();
    }

    public interface codeCorrectAndReadyListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onCodeCorrectAndReady();

    }

    private void initDefaultAttributes() {
        padding = getContext().getResources().getDimension(R.dimen.view_padding);

        bottomLineStrokeWidth = getContext().getResources().getDimension(R.dimen.bottomLine_stroke_width);//查API知此方法自动处理单位转换。
        bottomLineHorizontalLength = getContext().getResources().getDimension(R.dimen.bottomLine_horizontal_length);
        bottomLineHorizontalMargin = getContext().getResources().getDimension(R.dimen.bottomLine_horizontal_margin);
        bottomLineColor = ContextCompat.getColor(mContext,R.color.bottomLine_default_color);
        bottomLineNonCorrectColor = ContextCompat.getColor(mContext,R.color.bottomLine_nonCorrect_color);

        textSize = getContext().getResources().getDimension(R.dimen.text_size);
        textMarginBottom = getContext().getResources().getDimension(R.dimen.text_margin_bottom);
        textColor = ContextCompat.getColor(mContext,R.color.textColor);
        textNonCorrectColor = ContextCompat.getColor(mContext,R.color.text_nonCorrect_Color);
        backgroundColor = ContextCompat.getColor(mContext,R.color.ve_background);
        backgroundNotCorrectColor = ContextCompat.getColor(mContext,R.color.ve_background_not_correct);

        viewHeight = getContext().getResources().getDimension(R.dimen.view_height);
    }

    private void initCustomAttributes(AttributeSet attributeset) {
        TypedArray attributes =
                getContext().obtainStyledAttributes(attributeset, R.styleable.ValidatingEditor);

        bottomLineColor = attributes.getColor(R.styleable.ValidatingEditor_bottomLine_color, bottomLineColor);
        bottomLineNonCorrectColor =
                attributes.getColor(R.styleable.ValidatingEditor_bottomLine_nonCorrect_color, bottomLineNonCorrectColor);
        textColor = attributes.getInt(R.styleable.ValidatingEditor_text_color, textColor);
        textNonCorrectColor = attributes.getInt(R.styleable.ValidatingEditor_text_nonCorrect_color, textNonCorrectColor);

        attributes.recycle();
    }



    private void initPaint() {
        bottomLinePaint = new Paint();
        bottomLinePaint.setColor(bottomLineColor);
        bottomLinePaint.setStrokeWidth(bottomLineStrokeWidth);
        bottomLinePaint.setStyle(android.graphics.Paint.Style.STROKE);

        bottomLineNonCorrectPaint = new Paint();
        bottomLineNonCorrectPaint.setColor(bottomLineNonCorrectColor);
        bottomLineNonCorrectPaint.setStrokeWidth(bottomLineStrokeWidth);
        bottomLineNonCorrectPaint.setStyle(android.graphics.Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textNonCorrectPaint = new Paint();
        textNonCorrectPaint.setTextSize(textSize);
        textNonCorrectPaint.setColor(textNonCorrectColor);
        textNonCorrectPaint.setAntiAlias(true);
        textNonCorrectPaint.setTextAlign(Paint.Align.CENTER);

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(backgroundColor);

    }


    private void initViewOptions() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    //【学：据说是系统计算好控件的实际尺寸后以本方法通知用户】
    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        Log.i(TAG, "onSizeChanged: b");

        sizeChangedHeight = h;
        sizeChangedWidth = w;
        initUnderline(w);



        super.onSizeChanged(w, h, old_w, old_h);

    }

    //目前，宽度固定设置为最大值；高度视控件文本字符数量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.i(TAG, "onMeasure: b");
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);//这样设置的前提是XML中明确给控件设置为match_..
//        Log.i(TAG, "onMeasure: maxW = "+maxWidth);
        float requireHeight = viewHeight+padding*2;
        if (targetText.isEmpty()) {
//            Log.i(TAG, "onMeasure: text is empty");
            setMeasuredDimension(maxWidth, (int) requireHeight);
            return;
        }

        float requiredTotalWidth = bottomLineSectionAmount*bottomLineHorizontalLength+padding*2;
        if(requiredTotalWidth>maxWidth){
            lines = (int)(requiredTotalWidth/maxWidth)+1;
//            Log.i(TAG, "onMeasure: bottomLineSectionAmount*bottomLineHorizontalLength = "+(int)(bottomLineSectionAmount*bottomLineHorizontalLength));
//            Log.i(TAG, "onMeasure: lines = "+lines);
            requireHeight = viewHeight*lines+padding*2;
        }

        setMeasuredDimension(maxWidth,(int)requireHeight);

    }





    private void showKeyboard() {
        InputMethodManager inputmethodmanager =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmethodmanager.showSoftInput(this, InputMethodManager.RESULT_UNCHANGED_SHOWN);
        inputmethodmanager.viewClicked(this);
    }

    private void hideKeyBoard(){
        InputMethodManager inputmethodmanager = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputmethodmanager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }


    /**
     * Set Input type like InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_NUMBER
     * Doesn't work for password
     * @param inputType
     */
    public void setInputType(int inputType) {
        mInputType = inputType;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.actionLabel = null;
        outAttrs.inputType = TYPE_CLASS_TEXT;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new BaseInputConnection(this, true);
//        return new InnerInputConnection(super.onCreateInputConnection(outAttrs), false);
    }

    /*class InnerInputConnection extends InputConnectionWrapper implements InputConnection{
        public InnerInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean setSelection(int start, int end) {
            return super.setSelection(start, end);
        }
    }*/

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }


    /**
     * Detects the del key and delete characters
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && characters.size() != 0) {
            characters.pop();
            currentPosition--;
            if(currentPosition<leastWrongPosition){
                leastWrongPosition = 0;
            }
        }
        return super.onKeyDown(keyCode, keyevent);
    }

    /**
     * Capture the keyboard events, for inputs
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyevent) {
        String text = KeyEvent.keyCodeToString(keyCode);//返回的一定是KEYCODE_开头（已知字符）或数字1001（未知字符）

        if(!keyevent.isCapsLockOn()) {
            return inputText(text,false);
        }
        return inputText(text,true);
    }

    /**
     * String text
     * Pass empty string to remove text
     *
     * @param text text to input
     * @return true if input
     */
    private boolean inputText(String text, boolean capsOn) {
        Matcher matcher = KEYCODE_PATTERN.matcher(text);
        if (matcher.matches()) {
//            Log.i(TAG, "inputText: b");
            String matched = matcher.group(1);
            char character;
            if(!capsOn){
                character = matched.toLowerCase().charAt(0);
            }else {
                character = matched.charAt(0);
            }
            characters.push(character);

            if (characters.size() >= bottomLineSectionAmount ) {//满了
                Log.i(TAG, "inputText: full");
                if(getCurrentString().compareTo(targetText) == 0) {
                    if(listener != null) {
                        Log.i(TAG, "inputText: interface triggered");
                        listener.onCodeCorrectAndReady();
                    }
                }
            }else {
                currentPosition++;
                if(Character.compare(character,targetText.charAt(currentPosition-1))!=0){//此位置上字符输入不正确
                    if(leastWrongPosition==0){
                        leastWrongPosition = currentPosition;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * When a touch is detected the view need to focus and animate if is necessary
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
        if (motionevent.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            showKeyboard();
        }
        return super.onTouchEvent(motionevent);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        if(gainFocus){
//           showKeyboard();
        }else {
            hideKeyBoard();
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(targetText.isEmpty()) {
            Log.i(TAG, "onDraw: empty, ready return");
            return;
        }

        if(leastWrongPosition!=0) {
            backgroundPaint.setColor(backgroundNotCorrectColor);
        }else {
            backgroundPaint.setColor(backgroundColor);
        }
        canvas.drawRect(0,0,sizeChangedWidth,sizeChangedHeight,backgroundPaint);

        for (int i = 0; i < bottomLineSections.length; i++) {
//            Log.i(TAG, "onDraw: i="+i);
            BottomLineSection sectionPath = bottomLineSections[i];
            float fromX = sectionPath.getFromX() + bottomLineHorizontalMargin;
            float fromY = sectionPath.getFromY();
            float toX = sectionPath.getToX() - bottomLineHorizontalMargin;
            float toY = sectionPath.getToY();

            drawSection(fromX, fromY, toX, toY, canvas);
            if (characters.toArray().length > i && characters.size() != 0) {
                Boolean characterCorrect = isCharacterCorrectAtPosition(i);
                drawCharacter(characterCorrect, fromX, toX, fromY, characters.get(i), canvas);
            }
        }
//        Log.i(TAG, "onDraw: ready to invalidate");
        invalidate();
    }

    private boolean isCharacterCorrectAtPosition(int position){
        return characters.get(position).compareTo(targetText.charAt(position))==0;//相等，字符正确。

    }

    private void drawSection(float fromX, float fromY, float toX, float toY,
                             Canvas canvas) {
        Paint paint = bottomLinePaint;
        /*if (!correct) {
            paint = bottomLineNonCorrectPaint;
        }*/
        canvas.drawLine(fromX, fromY, toX, toY, paint);
    }

    private void drawCharacter(boolean correct, float fromX, float toX, float fromY, Character character, Canvas canvas) {
        Paint paint = textPaint;
        if(!correct){
            paint = textNonCorrectPaint;
        }
        float actualWidth = toX - fromX;
        float centerWidth = actualWidth / 2;
        float centerX = fromX + centerWidth;//似乎跟API文档对应不起来啊？不是从左下角为原点绘制？
        canvas.drawText(character.toString(), centerX, fromY - bottomLineStrokeWidth/2 - textMarginBottom, paint);
    }


    public String getCurrentString() {
        StringBuilder sbd = new StringBuilder();
        for (Character c :
                characters) {
            sbd.append(c);
        }

        return sbd.toString();
    }


    /*
    * 方法由程序调用，动态设置目标字串
    * */
    public void setTargetText(String targetText){
        Log.i(TAG, "setTargetText: b");
        this.targetText = targetText;

        bottomLineSectionAmount = targetText.length();
//        Log.i(TAG, "setTargetText: bottomLineSectionAmount = "+bottomLineSectionAmount);
        //由于要根据目标字串的字符数量来绘制控件，所以所有需要用到该数量的初始化动作都只能在此后进行
        initDataStructures();

//        invalidate();
    }

    private void initDataStructures() {
        bottomLineSections = new BottomLineSection[bottomLineSectionAmount];
        characters = new FixedStack();
        characters.setMaxSize(bottomLineSectionAmount);
    }

    private void initUnderline(int viewMaxWidth) {
//        Log.i(TAG, "initUnderline: b");
        if(lines ==1) {
//            Log.i(TAG, "initUnderline: line 1");
            for (int i = 0; i < bottomLineSectionAmount; i++) {
                bottomLineSections[i] = createPath(i, 1, bottomLineHorizontalLength);
            }
        }else {
//            Log.i(TAG, "initUnderline: lines > 1");
            int sectionsMaxAmountPerLine = (int)(viewMaxWidth/bottomLineHorizontalLength);
            for (int i = 0; i < bottomLineSectionAmount; i++) {
                int theLine = (i/sectionsMaxAmountPerLine)+1;
//                Log.i(TAG, "initUnderline: i = "+i+" the Line = "+theLine);
                int positionInLine = i%sectionsMaxAmountPerLine;
                bottomLineSections[i] = createPath(positionInLine, theLine, bottomLineHorizontalLength);
            }

        }
    }

    private BottomLineSection createPath(int position, int theLine, float sectionLength) {
        float fromX = sectionLength * (float) position + padding;
        float heightPerLine = (sizeChangedHeight-2*padding)/theLine;
        float fromY = heightPerLine*theLine+padding;
//        Log.i(TAG, "createPath: theLine = "+theLine+" Y = "+fromY+" X = "+fromX);
        return new BottomLineSection(fromX, fromY, fromX + sectionLength, fromY);
    }
}
