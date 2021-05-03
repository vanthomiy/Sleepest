import tensorflow
import tensorflow as tf
from SleepCalculation04DefineLite import *

def convertSaveModel(saved_model_dir, modelname, addData):
    # Convert the model
    converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir) # path to the SavedModel directory
    converter.target_ops = [tf.lite.OpsSet.TFLITE_BUILTINS, tf.lite.OpsSet.SELECT_TF_OPS]
    #converter.allow_custom_ops=True
    converter.experimental_new_converter =True    
    tflite_model = converter.convert()

    if(addData):
        tflite_model = addMetaData(tflite_model)

    # Save the model.
    with open(modelname, 'wb') as f:
        f.write(tflite_model)

def convertFromConcreteFunction(model):
    
        # (ro run your model) result = Squared(5.0) # This prints "25.0"
        # (to generate a SavedModel) tf.saved_model.save(model, "saved_model_tf_dir")
        concrete_func = model.__call__.get_concrete_function()

        # Convert the model
        converter = tf.lite.TFLiteConverter.from_concrete_functions([concrete_func])
        tflite_model = converter.convert()

        # Save the model.
        with open('model.tflite', 'wb') as f:
            f.write(tflite_model)

def convertFromKeras(model, name):
    
    # Create a model using high-level tf.keras.* APIs
    '''model = tf.keras.models.Sequential([
        tf.keras.layers.Dense(units=1, input_shape=[1]),
        tf.keras.layers.Dense(units=16, activation='relu'),
        tf.keras.layers.Dense(units=1)
    ])
    model.compile(optimizer='sgd', loss='mean_squared_error') # compile the model
    model.fit(x=[-1, 0, 1], y=[-3, -1, 1], epochs=5) # train the model'''
    # (to generate a SavedModel) tf.saved_model.save(model, "saved_model_keras_dir")

    # Convert the model.
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()

    # Save the model.
    with open(name, 'wb') as f:
        f.write(tflite_model)

def convertSaveModelWithCustomOps(saved_model_dir, modelname, addData):
    # Convert the model
    converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir) # path to the SavedModel directory
    converter.target_ops = [tf.lite.OpsSet.TFLITE_BUILTINS, tf.lite.OpsSet.SELECT_TF_OPS]
    converter.allow_custom_ops=True
    converter.experimental_new_converter =True    
    tflite_model = converter.convert()

    if(addData):
        tflite_model = addMetaData(tflite_model)

    # Save the model.
    with open(modelname, 'wb') as f:
        f.write(tflite_model)

