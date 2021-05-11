import math
import numpy as np
import pandas as pd
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
import kerasdataset as kd
from ConvertModel import *

def create_deep_and_cross_model():

    inputs = kd.create_model_inputs()
    x0 = kd.encode_inputs(inputs, use_embedding=True)

    cross = x0
    for _ in kd.hidden_units:
        units = cross.shape[-1]
        x = layers.Dense(units)(cross)
        cross = x0 * x + cross
    cross = layers.BatchNormalization()(cross)

    deep = x0
    for units in kd.hidden_units:
        deep = layers.Dense(units)(deep)
        deep = layers.BatchNormalization()(deep)
        deep = layers.ReLU()(deep)
        deep = layers.Dropout(kd.dropout_rate)(deep)

    merged = layers.concatenate([cross, deep])
    outputs = layers.Dense(units=kd.NUM_CLASSES, activation="softmax")(merged)
    model = keras.Model(inputs=inputs, outputs=outputs)
    return model


deep_and_cross_model = create_deep_and_cross_model()

deep_and_cross_model.save('deep_and_cross_model')

convertSaveModel('deep_and_cross_model', 'deep_and_cross_model.tflite', False)
#convertFromKeras(deep_and_cross_model, 'deep_and_cross_model.tflite')

keras.utils.plot_model(deep_and_cross_model, show_shapes=True, rankdir="LR")


