from tflite_support import flatbuffers
from tflite_support import metadata as _metadata
from tflite_support import metadata_schema_py_generated as _metadata_fb


def addMetaData(model_file):

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

    # Creates output info.
    output_meta = _metadata_fb.TensorMetadataT()
    output_meta.name = "sleepState"
    output_meta.description = "Sleep State of the raw api data with 0 = awake or 1 = sleeping"


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

    populator = _metadata.MetadataPopulator.with_model_file(model_file)
    populator.load_metadata_buffer(metadata_buf)
    populator.populate()

    return model_file