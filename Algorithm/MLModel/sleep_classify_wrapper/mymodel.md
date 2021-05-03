# MyModel Usage

```
import org.tensorflow.lite.classify.MyModel;

// 1. Initialize the Model
MyModel model = null;

try {
    model = new MyModel(context);  // android.content.Context
    // Create the input container.
    MyModel.Inputs inputs = model.createInputs();
} catch (IOException e) {
    e.printStackTrace();
}

if (model != null) {

    // 2. Set the inputs
    // Load input tensor "sleeprawapidataset" from a Bitmap with ARGB_8888 format.
    Bitmap bitmap = ...;
    inputs.loadSleeprawapidataset(bitmap);
    // Alternatively, load the input tensor "sleeprawapidataset" from a TensorImage.
    // Check out TensorImage documentation to load other image data structures.
    // TensorImage tensorImage = ...;
    // inputs.loadSleeprawapidataset(tensorImage);

    // 3. Run the model
    MyModel.Outputs outputs = model.run(inputs);

    // 4. Retrieve the results
    Map<String, Float> sleepstate = outputs.getSleepstate();
}
```
