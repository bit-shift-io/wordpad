package bitshift.wordpad;

import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/*
this should manage the edit text, line numbers and document

 */
public class DocumentViewMgr implements TextWatcher, View.OnLayoutChangeListener, Document.DocumentListener, SettingsMgr.SettingsMgrListener
{
    static private DocumentViewMgr mSingleInstance;
    private final EditText mEditTextView; // our document
    private final TextView mLineNumberView; // our line numbers
    private final Document mDocument;
    private final SettingsMgr mSettingsMgr;
    private int mLastLineCount = 0;

    final Handler mLineNumberHandler = new Handler();
    final Runnable mDelayLineNumber = new Runnable()
    {
        public void run()
        {
            updateLineNumbersDelayed();
        }
    };

    DocumentViewMgr(EditText editText, TextView lineNumberView)
	{
        mSingleInstance = this;
        mEditTextView = editText;
        mLineNumberView = lineNumberView;
        mDocument = Document.instance();
        mSettingsMgr = SettingsMgr.instance();
        ConfigureViews();
        createListeners();
    }

    static DocumentViewMgr instance()
    {
        return mSingleInstance;
    }

    private ArrayList<DocumentViewMgrListener> mListeners = new ArrayList<DocumentViewMgrListener> ();


    @Override
    public void onDocumentBodyChanged()
    {
        setText(mDocument.body());
    }

    @Override
    public void onDocumentSaved()
    {

    }

    @Override
    public void onDocumentOpened()
    {

    }

    @Override
    public void onSettingsChanged(SharedPreferences sharedPreferences, String key)
    {
        ConfigureViews();
    }

    // custom listener interface (interface is a group of related methods with empty bodies)
    // empty shell, this gets overriden by mainactivity
    public interface DocumentViewMgrListener
    {
        void onEditTextChanged();
    }

    public void addDocumentChangeListener(DocumentViewMgrListener listener)
    {
        mListeners.add(listener);
    }

    public void removeDocumentChangeListener(DocumentViewMgrListener listener)
    {
        mListeners.remove(listener);
    }

    void notifyEditTextChanged()
    {
        for (DocumentViewMgrListener listener : mListeners)
            listener.onEditTextChanged();
    }

    void createListeners()
    {
        mEditTextView.addTextChangedListener(this);
        mEditTextView.addOnLayoutChangeListener(this);
    }

    void destroyListeners()
    {
        mEditTextView.removeTextChangedListener(this);
        mEditTextView.removeOnLayoutChangeListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
    {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
    {

    }

    @Override
    public void afterTextChanged(Editable editable)
    {
        mDocument.setBody(mEditTextView.getText().toString()); // silent version of setBody!

        // compare line count before and after, see if there is any change
        int lineCount = getLineCount(mEditTextView);
        if (lineCount != mLastLineCount)
        {
            mLastLineCount = lineCount;
            updateLineNumbers();
        }

        notifyEditTextChanged();
    }

    @Override
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8)
    {
        updateLineNumbers();
    }

    int getLineCount(EditText editText)
    {
        Layout layout = editText.getLayout();

        if (layout == null)
            return 0;
        else
            return layout.getLineForOffset(editText.length());
    }

    // we need to delay this because of android bugs
    void updateLineNumbers()
    {
        mLineNumberHandler.removeCallbacks(mDelayLineNumber);
        mLineNumberHandler.postDelayed(mDelayLineNumber, 500);
    }

    // calculate line numbers and put in a text field
    void updateLineNumbersDelayed()
    {
        if (!mSettingsMgr.lineNumbers())
            return;

        if (!mSettingsMgr.wordWrap())
        {
            StringBuilder lineNumbers = new StringBuilder();
            lineNumbers.append("1\n"); // we always have our start line

            for (int i = 0 ; i < mEditTextView.getLineCount(); ++i)
                lineNumbers.append(String.valueOf(i + 1) + "\n");

            mEditTextView.setText(lineNumbers);
            return;
        }

        Layout layout = mEditTextView.getLayout();
        if (layout == null)
        {
            mLineNumberView.setText("1\n");
            return;
        }

        // Bellow code for if wordwrap is enabled
        // First find line segments
        List<Integer> lineArray = new ArrayList<Integer>(); // this array will hold a start and end position for each line segment
        lineArray.add(0); // add start line position 0 based

        String body = mDocument.body();
        int bodyLength = body.length();

        // store position of each new line in an array
        for(int pos = 0; pos < bodyLength; ++pos)
        {
            char c = body.charAt(pos);

            if(c == '\r')
            {
                lineArray.add(pos + 1);

                if (pos+1 < bodyLength && body.charAt(pos+1) == '\n')
                    ++pos;
            }
            else if(c == '\n')
            {
                lineArray.add(pos + 1);
            }
        }

        // get the new line numbers and add to the line numbers view
        StringBuilder lineNumbers = new StringBuilder();
        for (int i = 0; i < lineArray.size(); ++i)
        {
            int lastLine = lineArray.get(Math.max(0, i - 1));
            int newLine = lineArray.get(i);

            // how many new lines do we need to add
            int blankLines = layout.getLineForOffset(newLine) - layout.getLineForOffset(lastLine);

            // insert blank new lines before our line number
            for (int z = 0 ; z < blankLines; ++z)
                lineNumbers.append("\n");

            // add our line number
            lineNumbers.append(String.valueOf(i + 1));
        }

        mLineNumberView.setText(lineNumbers);
    }

    void setText(String string)
    {
        destroyListeners(); // so we dont have updates etc when we change the edit text

        try
        {
            mEditTextView.setText(string); // this crashes if txt is huge
        }
        catch(OutOfMemoryError e)
        {
            e.printStackTrace();
            ToastMgr.instance().displayToast(R.string._out_of_memory);
        }

        createListeners();
    }

    void ConfigureViews()
    {
        // read only
        if (mSettingsMgr.readOnly())
        {
            mEditTextView.setClickable(false);
            mEditTextView.setCursorVisible(false);
            mEditTextView.setFocusable(false);
            mEditTextView.setFocusableInTouchMode(false);
            KeyboardMgr.instance().hideKeyboard();
        }
        else
        {
            mEditTextView.setClickable(true);
            mEditTextView.setCursorVisible(true);
            mEditTextView.setFocusable(true);
            mEditTextView.setFocusableInTouchMode(true);
        }

        // spelling text|textCapSentences|textMultiLine
        if (mSettingsMgr.checkSpelling())
            mEditTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        else
            mEditTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        // word wrap
        mEditTextView.setHorizontallyScrolling(!mSettingsMgr.wordWrap());

        // line numbers
        if (mSettingsMgr.lineNumbers())
        {
            mEditTextView.setPadding(85, 4, 4, 4);
            mLineNumberView.setVisibility(View.VISIBLE);
        }
        else
        {
            mEditTextView.setPadding(4, 4, 4, 4);
            mLineNumberView.setText("");
            mLineNumberView.setVisibility(View.GONE);
        }

        // font
        mEditTextView.setTypeface(mSettingsMgr.fontTypeface(), 0);
        mLineNumberView.setTypeface(mSettingsMgr.fontTypeface(), 0);

        // font size
        mEditTextView.setTextSize(mSettingsMgr.fontSize());
        mLineNumberView.setTextSize(mSettingsMgr.fontSize());

        // font color
        mEditTextView.setTextColor(mSettingsMgr.fontColor());
        mLineNumberView.setTextColor(mSettingsMgr.lineColor());
    }

    void requestFocus()
    {
        mEditTextView.requestFocus();
    }

    void clearFocus()
    {
        mEditTextView.clearFocus();
    }
}