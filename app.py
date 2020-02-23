# -*- coding: utf-8 -*-
"""
Created on Thu Jan  9 09:58:25 2020

@author: Tanmay
"""

from flask import Flask, jsonify, request
import numpy as np
import pickle
import pandas as pd
from sklearn.preprocessing import StandardScaler
import json
import pyrebase
import requests

app = Flask(__name__)

config = {
    "apiKey": "AIzaSyBgbUPkmRWjcLzgT49j8AMP8NpIfStUX_w",
    "authDomain": "helloworld-ffcd1.firebaseapp.com",
    "databaseURL": "https://helloworld-ffcd1.firebaseio.com",
    "projectId": "helloworld-ffcd1",
    "storageBucket": "helloworld-ffcd1.appspot.com",
    "messagingSenderId": "843339588996",
    "appId": "1:843339588996:web:41021eb028e8c3aa20a9d1",
    "measurementId": "G-W7Y5RS7LRG"
    }

firebase = pyrebase.initialize_app(config)

db = firebase.database()

@app.route('/predict', methods=['POST'])
def apicall():        
    loaded_model = pickle.load(open('sih_model_svm.pkl', 'rb'))
    jsondata = request.get_json()
    jdata = json.loads(jsondata)
    df = pd.read_json(jsondata,orient = "records")
    df_main = pd.read_csv('./features.txt')
    for i in range(1,len(df),10):    # step size is 10 means aggregrating 10 data pts means 1 second data
                if(i+9 > len(df)):
                    break
        		#print(i)
                dt = df[i-1:i+10]      # chunking the given dataframe into smaller dataframe containing 10 pts
                # start = dt.timestamp[i-1]
                # end = dt.timestamp[i+9]
        
        
        		# time-domain features : mean , max , min , var , std dev, median , interquartile range,
        		#                       mean of abs deviation , skewness < left : root mean sq error , entropy       
        		# mean 
                a = dt.mean()      # will give an array of mean of columns of dt
                mean_ax = a[0]
                mean_ay = a[1]
                mean_az = a[2]
        
                mean_gx = a[3]
                mean_gy = a[4]
                mean_gz = a[5]
                mean_speed = a[8]
        
        		# min
                a = dt.min()
                min_ax = a[0]
                min_ay = a[1]
                min_az = a[2]
        
                min_gx = a[3]
                min_gy = a[4]
                min_gz = a[5]
        
        		# max
                a = dt.max()
                max_ax = a[0]
                max_ay = a[1]
                max_az = a[2]
        
                max_gx = a[3]
                max_gy = a[4]
                max_gz = a[5]
        
        		# std dev
                a = dt.std()
                sd_ax = a[0]
                sd_ay = a[1]
                sd_az = a[2]
        
                sd_gx = a[3]
                sd_gy = a[4]
                sd_gz = a[5]
        
        		# variance
                a = dt.var()
                var_ax = a[0]
                var_ay = a[1]
                var_az = a[2]
        
                var_gx = a[3]
                var_gy = a[4]
                var_gz = a[5]
        
            
        		# interquantile ranges
                a = dt.quantile(.25)
                quant1_ax = a[0]
                quant1_ay = a[1]
                quant1_az = a[2]
        
                quant1_gx = a[3]
                quant1_gy = a[4]
                quant1_gz = a[5]
        
        		# mean absolute deviation
                a = dt.mad()
                mad_ax = a[0]
                mad_ay = a[1]
                mad_az = a[2]
        
                mad_gx = a[3]
                mad_gy = a[4]
                mad_gz = a[5]
        
        		# adding latitude and longitude
                latitude = dt['latitude'][i+4]
        
        
                longitude = dt['longitude'][i+4]
        
                df_temp = pd.DataFrame([[mean_ax,mean_ay,mean_az,mean_gx,mean_gy,mean_gz,sd_ax,
        		                         sd_ay,sd_az,sd_gx,sd_gy,sd_gz,min_ax,min_ay,min_az,min_gx,min_gy,min_gz,
        		                         max_ax,max_ay,max_az,max_gx,max_gy,max_gz,var_ax,var_ay,var_az,var_gx,var_gy,
        		                         var_gz,quant1_ax,quant1_ay,quant1_az,
        		                         quant1_gx,quant1_gy,quant1_gz,
    		                             mad_ax,mad_ay,mad_az,mad_gx,mad_gy,mad_gz,mean_speed,
    		                             latitude,longitude]], 
        
        		                      columns = ('mean_ax','mean_ay','mean_az','mean_gx','mean_gy',
        		                                 'mean_gz','sd_ax','sd_ay','sd_az','sd_gx','sd_gy','sd_gz','min_ax','min_ay',
        		                                 'min_az',
        		                                 'min_gx','min_gy','min_gz','max_ax','max_ay','max_az','max_gx','max_gy','max_gz',
        		                                 'var_ax','var_ay','var_az','var_gx','var_gy','var_gz',
        		                                 'quant1_ax','quant1_ay','quant1_az','quant1_gx',
        		                                 'quant1_gy',
        		                                 'quant1_gz',
        		                                 'mad_ax','mad_ay','mad_az','mad_gx','mad_gy','mad_gz','mean_speed',
    		                                     'latitude', 'longitude'))
        
                df_main = df_main.append(df_temp)
    
    
    	# putting time stamps at the end
    cols = list(df_main.columns.values) #Make a list of all of the columns in the df
    cols.pop(cols.index('latitude')) # remove latitude
    cols.pop(cols.index('longitude')) # remove longitude
    df_main = df_main[cols+['latitude', 'longitude']]  
      
            #data = np.array(df_main)
        
    x = df_main.iloc[:,0:-2]
    y = df_main.iloc[:,-2:]
    y.reset_index(inplace = True)   
    y.drop(['index'], inplace=True, axis = 1) 
    
        	# Data-preprocessing: Standardizing the data matrix 'x'
    standardized_data = StandardScaler().fit_transform(x)
        	# coln std our feature matrix 
    x = standardized_data
    y_pred = loaded_model.predict(x)
    y_df = pd.DataFrame(data = y_pred)
    y_final = pd.concat([y,y_df], axis = 1)
    prediction_series = list(pd.Series(y_pred))
    final_predictions = pd.DataFrame(prediction_series)
    responses = jsonify(y_final.to_json(orient="records"))
    for i in range(0,len(y_pred)):
        if(y_pred[i] == 1.0 or y_pred[i] == 2.0):
            db.child("reports").push((y.iloc[[i]].to_dict(orient = 'records'))[0])
    responses.status_code = 200

    return (responses)

if __name__ == '__main__':
    app.run(port = 5000, debug=True)


