from tflite_support import flatbuffers
from tflite_support import metadata as _metadata
from tflite_support import metadata_schema_py_generated as _metadata_fb
import pandas as pd

import os

def addMetaData():

    txt_file = 'datasets/label04.txt' 
    tflite_file = 'sleep_classifier_model.tflite' 

    # Creates model info.
    model_meta = _metadata_fb.ModelMetadataT()
    model_meta.name = "Sleep-Raw-Api Sleep-classifier"
    model_meta.description = ("Identify if the user is sleeping or not "
                            "uses the last 10 sleep api raw datasets")
    model_meta.version = "v1"
    model_meta.author = "Thomas Kreidl"
    model_meta.license = ("Apache License. Version 2.0 "
                        "http://www.apache.org/licenses/LICENSE-2.0.")

    
    # Creates input info.
    input_meta = _metadata_fb.TensorMetadataT()
    input_meta.name = "sleepRawApiDataSet"
    input_meta.description = (
        "Input Sleep Raw Api Data to be classified. It should be the last 10 Datasets with"+
        "Time, Real, light0, motion0, sleep0,light1,motion1,sleep1,....,sleep10")
    input_meta.content = _metadata_fb.ContentT()
    input_meta.content.contentProperties = _metadata_fb.image()
    input_meta.content.contentProperties.colorSpace = (
        _metadata_fb.ColorSpaceType.RGB)
    input_meta.content.contentPropertiesType = (
        _metadata_fb.ContentProperties.ImageProperties)
    input_normalization = _metadata_fb.ProcessUnitT()
    input_normalization.optionsType = (
        _metadata_fb.ProcessUnitOptions.NormalizationOptions)
    input_normalization.options = _metadata_fb.NormalizationOptionsT()
    input_normalization.options.mean = [127.5]
    input_normalization.options.std = [127.5]
    input_meta.processUnits = [input_normalization]
    input_stats = _metadata_fb.StatsT()
    input_stats.max = [255]
    input_stats.min = [0]
    input_meta.stats = input_stats


    # Creates output info.
    output_meta = _metadata_fb.TensorMetadataT()
    output_meta.name = "sleepState"
    output_meta.description = "Sleep State of the raw api data with 0 = awake or 1 = sleeping"
    
    output_meta.content = _metadata_fb.ContentT()
    output_meta.content.content_properties = _metadata_fb.FeaturePropertiesT()
    output_meta.content.contentPropertiesType = (
        _metadata_fb.ContentProperties.FeatureProperties)
    output_stats = _metadata_fb.StatsT()
    output_stats.max = [1.0]
    output_stats.min = [0.0]
    output_meta.stats = output_stats
    label_file = _metadata_fb.AssociatedFileT()
    label_file.name = os.path.basename(txt_file)
    label_file.description = "Labels for objects that the model can recognize."
    label_file.type = _metadata_fb.AssociatedFileType.TENSOR_AXIS_LABELS
    output_meta.associatedFiles = [label_file]

    
    # Creates subgraph info
    subgraph = _metadata_fb.SubGraphMetadataT()
    subgraph.inputTensorMetadata = [input_meta]
    subgraph.outputTensorMetadata = [output_meta]
    model_meta.subgraphMetadata = [subgraph]
    

    
    b = flatbuffers.Builder(0)
    b.Finish(
        model_meta.Pack(b),
        _metadata.MetadataPopulator.METADATA_FILE_IDENTIFIER)
    metadata_buf = b.Output()
    

    

    populator = _metadata.MetadataPopulator.with_model_file(tflite_file)
    populator.load_metadata_buffer(metadata_buf)
    populator.load_associated_files([txt_file])
    populator.populate()




addMetaData()