import model_search
from model_search import constants
from model_search import single_trainer
from model_search.data import csv_data


csv_file = 'datasets/sleepdata04v1.csv' 

trainer = single_trainer.SingleTrainer(
    data=csv_data.Provider(
        label_index=0,
        logits_dimension=2,
        record_defaults=[0, 0, 0, 0],
        filename=csv_file),
    spec=constants.DEFAULT_DNN)

trainer.try_models(
    number_models=200,
    train_steps=1000,
    eval_steps=100,
    root_dir="/tmp/run_example_model",
    batch_size=32,
    experiment_name="model04",
    experiment_owner="model_search_user")