# FakeView
English Document.

顾名思义，假的View。

开发Android应用时，特别是开发一个通用列表时，列表中基本只使用ImageView和TextView，且不会使用其中很多功能。

FakeView就是为了提取出日常使用较多，且容易造成layout层级过多或过度绘制而导致页面卡顿（主要是滑动卡顿）的View，经过处理后，合并层级，减少过度绘制。

附带减少view层级的[工具](Tools.MD)

0.50版本新增[NewText](NewText.MD)

## 原理
FakeView提取出在屏幕绘制一个控件最基本的动作：创建-计算-布局-绘制，并将其封装为FView和FViewGroup，分别模拟View和ViewGroup；提取出根调度器FViewRootImpl（View子类）模拟ViewRootImpl，调度"计算"、"布局"、"绘制"等动作，并将所有FView加进FViewRootImpl，实现合并图层，减少过度绘制。

## 使用方法
gradle:
```
compile "xfy.fakeview.library:fview:0.62"
```
直接生成FView：
```
@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = (ViewGroup) findViewById(R.id.container);
        container.addView(buildFView());
}

private View buildFView() {
        FViewCreater creater = new FViewCreater(this);
        FFrameLayout layout = creater.buildRootFView(FFrameLayout.class);
        layout.setFLayoutParams(newMatchParentParams());
        layout.setId(1);
        layout.setOnClickListener(this);
        layout.setOnLongClickListener(this);

        final FImageView imageView = creater.newFView(FImageView.class);
        imageView.setPadding(10, 10, 10, 10);
        imageView.setImageResource(R.drawable.me);
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);
        imageView.setId(2);
        imageView.setBackgroundColor(Color.BLACK);

        layout.addView(imageView, new FFrameLayout.LayoutParams(IFViewGroup.FLayoutParams.WRAP_CONTENT,
                IFViewGroup.FLayoutParams.WRAP_CONTENT, Gravity.CENTER));

        final FTextView textView = creater.newFView(FTextView.class);
        textView.setPadding(10, 10, 10, 10);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setTextColor(Color.RED);
        textView.setBackgroundColor(Color.YELLOW);
        textView.setText(SpannableTextUtils.getColorableText("哈哈哈哈哈哈哈哈哈", new int[] {0, 2}, new int[]{1, 4}, new int[]{Color.BLACK, Color.BLUE}));

        layout.addView(textView, new FFrameLayout.LayoutParams(IFViewGroup.FLayoutParams.WRAP_CONTENT,
                IFViewGroup.FLayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER)
        .setMargins(0, 0, 0, 20));

        View root = creater.build();
        root.setLayoutParams(FViewCreater.newMatchParentLayoutParams());

        return root;
    }

//implement IFView.OnClickListener
@Override
public void onClick(IFView view) {
        switch (view.getId()) {
            case 1:
                Toast.makeText(this, "container click", Toast.LENGTH_LONG).show();
                break;
            case 2:
                startActivity(new Intent(this, TestTranslateActivity.class));
                break;
        }
}
```
将已有layout翻译为FView：
```
TranslatorManager translatorManager = new TranslatorManager(this);
final ViewGroup container = (ViewGroup) findViewById(R.id.container);
final IFViewRoot root = translatorManager.translateView(container);
if(root != null)
    Log.d(tag, "translate success");
```
如果有点击或长按事件，需要在翻译前设置事件：
```
private OnClickListener onClickListener = new OnClickListener(this);//this -> OnClickListener.Trans
private OnLongClickListener onLongClickListener = new OnLongClickListener(this);//this -> OnLongClickListener.Trans

//implement OnClickListener.Trans
@Override
public void onClick(IView view) {
        if (view == null) {
            showToast("null view");
        }
        switch (view.getId()) {
            case R.id.xxxx:
                break
        }
}

private void initEvent() {
        findViewById(R.id.xxxx).setOnClickListener(onClickListener);
        findViewById(R.id.xxxx).setOnLongClickListener(onLongClickListener);
}
```
如需调试，可设置`DebugInfo.setDebug(true);`
## 最后
迎提出意见及建议。

email: s18810577589@sina.com