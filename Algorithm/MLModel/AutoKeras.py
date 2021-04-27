import pandas as pd
from keras.models import Sequential
from keras.layers import *
from sklearn.model_selection import train_test_split
df = pd.read_csv("./generated_dataset.csv")

#split of dataset to input set X and output set Y
X = df.drop(labels=['y'], axis=1).values
Y = df[['y']].values

#split of the X&Y sets to training and test X&Y sets with ratio 8:2
X_train, X_test, Y_train, Y_test = train_test_split(X,Y,test_size=0.2, random_state=42)

#Model definition
model = Sequential()
model.add(Dense(16, input_dim=6, activation='relu'))
model.add(Dense(1, activation='linear'))
model.compile(loss='mean_squared_error', optimizer='adam', metrics=['accuracy'])

#Run training
model.fit(X_train, Y_train, epochs=40, shuffle=True, verbose=2)

#Lets test the model error rate
#test_set_error_rate = model.evaluate(X_test, Y_test, verbose=0)
#print("The mean squared error (MSE) for the test data set is: {}".format(test_set_error_rate))
# Manual test
X_prediction= pd.DataFrame([{'x1':1,'x2':2,'x3':3,'x4':4,'x5':5,'x6':6}])
print( model.predict(X_prediction) )

# Save the model to disk
model.save("trained_model.h5", include_optimizer=False)