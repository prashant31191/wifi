package hillfly.wifichat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class HandyTextView extends TextView {
	//Context mContext;

	public HandyTextView(Context context) {

		super(context);
		//		mContext = context;
	}

	public HandyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//mContext = context;
	}

	public HandyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//mContext = context;
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		if (text == null) {
			text = "";
		}
		//HandyTextView handyTextView = null;
		/*if(mContext !=null)
		{
			Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),"fonts/AlexBrush-Regular.ttf"); 
			super.setTypeface(typeface);
		}  
*/
		super.setText(text, type);
	}
}
