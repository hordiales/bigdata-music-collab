#!/usr/bin/python2
# -*- coding: UTF-8 -*-

from __future__ import print_function
import flask
from flask import jsonify, request
from flask_autodoc import Autodoc
import logging
import os
import json
from flask import abort
import itertools
import random
import freesound


DATA_PATH = "./data"

app = flask.Flask(__name__)
auto = Autodoc(app)

#########################
# Helper functions
#########################


#########################
# API functions
#########################

@app.route('/documentation')
def documentation():
   """
       Doc generation
   """
   return auto.html('public').replace("&amp;lt;br&amp;gt;","") # quick fix with replace (html gen)

soundsdir = "/sounds"
# soundsdir = "/pistas"

@auto.doc('public')
@app.route(soundsdir+'/<int:id>', methods=['GET'])
def get_pista(id):
    """
        Info de la pista (json)
    """
    try:
        desc = json.load( open(DATA_PATH + "/" + str(id) + ".json",'r') )
        return jsonify(desc)
    except:
        abort(404) #not found

@auto.doc('public')
@app.route(soundsdir+'/<int:id>/audio', methods=['GET'])
def get_pista_audio(id):
    """
        Retorna el full path donde esta descargado el audio con ese id
    """
    try:
        try:
            # try to find it locally
            desc = json.load( open(DATA_PATH + "/" + str(id) + ".json",'r') )
            return flask.send_file( DATA_PATH + "/" + desc['filename'] )
        except:
            print("Downloading from freesound.org first...")
            # Freesound setup
            auth_header = request.headers['Authorization']
            api_key = auth_header.split(' ')[1]
            print("API KEY: "+api_key)
            freesound_client = freesound.FreesoundClient()
            freesound_client.set_token(api_key)

            try:
                sound = freesound_client.get_sound(id,fields="id,name,previews")
                sound.retrieve_preview(DATA_PATH, sound.name)
                print(sound.name + " - ID: "+str(sound.id)) 

                sound_json = dict()
                sound_json['id'] = sound.id
                sound_json['name'] = sound.name
                sound_json['filename'] = sound.name
                with open( DATA_PATH+'/'+str(sound.id)+'.json', 'w') as outfile:
                    json.dump(sound_json, outfile)
                print("New json created")
            except Exception, e:
                print(e)
                raise Exception("freesound api error")

            return flask.send_file( DATA_PATH + "/" + sound.name )
    except:
        abort(404) #not found

@auto.doc('public')
@app.route('/search', methods=['POST'])
def post_search():
    """
        Makes the search and returns the ID
    """
    try:
        request_json     = request.get_json()
        bpm           = request_json.get('BPM')
        duration      = request_json.get('duration')
        spectral_centroid = request_json.get('spectral_centroid')
        inharmonicity = request_json.get('inharmonicity')

        # print(bpm, duration, spectral_centroid, inharmonicity)
        response_content = None

        # Freesound setup
        auth_header = request.headers['Authorization']
        api_key = auth_header.split(' ')[1]
        # print("API KEY: "+api_key)
        freesound_client = freesound.FreesoundClient()
        freesound_client.set_token(api_key)

        # bpm = 120
        # inharmonicity = 10
        duration = float(duration)
        print("Max duration: %f"%duration)

        # Content based search example
        print("Content based search:")
        print("---------------------")
        results_pager = freesound_client.content_based_search(
            # page_size=10,
            descriptors_filter="sfx.duration:[* TO %f]"%(duration),
            #descriptors_filter="sfx.duration:[* TO %f] sfx.inharmonicity:[* TO %f]"%(duration,inharmonicity),
            # target='rhythm.bpm: %f lowlevel.spectral_centroid: %f'%(bpm,spectral_centroid)
        )

        # results_pager = freesound_client.content_based_search(
        #     descriptors_filter="lowlevel.pitch.var:[* TO 20]",
        #     target='lowlevel.pitch_salience.mean:1.0 lowlevel.pitch.mean:440'
        # )

        print("Num results:", results_pager.count)
        for sound in results_pager:
            print("\t-", sound.name, "by", sound.username)
        print()

        #Note: min between results and page size to avoid get newer pages
        #selected = min( random.randint(0,results_pager.count-1), 10-1)
        selected = random.randint(0,10-1) #FIXME
        print("Selected: %i"%selected)
        sound = results_pager[ selected ]
        response_content = dict()
        try:
            response_content['id'] = sound.id
            response_content['name'] = sound.name
            try:
                response_content['description'] = sound.description
            except:
                max_tags = 5
                i = 0
                response_content['description'] = ""
                for tag in sound.tags:
                    if i>=max_tags:
                        break
                    response_content['description'] += str(tag)+', '
                    i += 1
                response_content['description'] = response_content['description'][:-2
                ]
            response_content['license'] = sound.license
        except:
            pass
        print(response_content)
        return jsonify(response_content)
    except:
        abort(404) #not found

if __name__ == "__main__":
    file_handler = logging.FileHandler('mock_api_ws.log')
    app.logger.addHandler(file_handler)
    app.logger.setLevel(logging.INFO)

    random.seed()
    #app.run()
    app.run( debug=True, host="0.0.0.0", port=5000 )
