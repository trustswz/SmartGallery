package imagic.mobile.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class MagicTextView extends TextView {

	private float strokeWidth = 1;
	private Integer strokeColor = 0;
	private Join strokeJoin = Join.ROUND;
	private float strokeMiter = 5;

	public MagicTextView(Context context) {
		super(context);
	}
	public MagicTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MagicTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setStroke(float width, int color, Join join, float miter){
		strokeWidth = width*this.getTextSize()/20;
		if(strokeWidth < 1){
			strokeWidth = 1;
		}
		strokeColor = color;
		strokeJoin = join;
		strokeMiter = miter;
	}

	@Override
	public void onDraw(Canvas canvas){

		final ColorStateList textColor = getTextColors();

		TextPaint paint = this.getPaint();
		
		paint.setStyle(Style.STROKE);
		paint.setStrokeJoin(strokeJoin);
		paint.setStrokeMiter(strokeMiter);
		this.setTextColor(strokeColor);
		paint.setStrokeWidth(strokeWidth);

		super.onDraw(canvas);

		paint.setStyle(Style.FILL);

		this.setTextColor(textColor);
		super.onDraw(canvas);
	}
}