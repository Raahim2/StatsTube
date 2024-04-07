#----------------------------IMPORTING-----------------------------#
import streamlit as st
from transformers import pipeline
from diffusers import DiffusionPipeline
from ctransformers import AutoModelForCausalLM
from diffusers.utils import export_to_gif
import os


#-----------------------------LOADING MODELS-----------------------------#
@st.cache_resource
def load_classifier():
    path = "Models/Text-Classifier-Model"
    if os.path.exists(path):
        classifier = pipeline(task="text-classification"  , model=path)
    else:
        classifier = pipeline(task="text-classification" )
    return classifier


@st.cache_resource
def load_chatbot(offline_path , online_path):
    if os.path.exists(offline_path):
        chatbot = AutoModelForCausalLM.from_pretrained(offline_path)
    else:
        if(online_path=="openai-community/gpt2"):
            chatbot = pipeline("text-generation", model=online_path)
        else:    
            chatbot = AutoModelForCausalLM.from_pretrained(online_path)
    return chatbot


@st.cache_resource
def load_stable_diffuser():
    path = "Models/Stable-Diffuser-Img-Generator-Model"
    if os.path.exists(path):
        generator = DiffusionPipeline.from_pretrained(path)
    else:
        generator = DiffusionPipeline.from_pretrained("stabilityai/stable-diffusion-xl-base-1.0")
    return generator

@st.cache_resource
def load_summrizer():
    summrizer = pipeline("summarization", model="Falconsai/medical_summarization")
    return summrizer

def load_3d():
    model3d = DiffusionPipeline.from_pretrained("openai/shap-e")
    return model3d

#-----------------------------MODEL WORKING-----------------------------#


def Classify_Text(inp ):
    classifier = load_classifier()
    a=classifier([inp])
    return a


def Generate_Text(prompt , placeholder , offline_path , online_path):
    bot = load_chatbot(offline_path , online_path)
    gen_txt=""
    for text in bot(f"Chat : {prompt}  Reply : " ,  stop=['Chat : ']  , stream=True):
        print(text , end="")
        gen_txt = gen_txt + text
        placeholder.markdown(gen_txt ,unsafe_allow_html=True)
    return gen_txt



def Generate_Image(prompt , num):
    generator = load_stable_diffuser()
    im =  generator(prompt , num_inference_steps=num).images[0]
    return im

def Summrize_Text(prompt):
    summrizer = load_summrizer()
    summrized_text = summrizer(prompt , max_length=2000)
    return summrized_text

def Turn_To_3D(prompt):
    model = load_3d()
    images = model(prompt, num_inference_steps=10, size=256,).images
    gif_path = export_to_gif(images, "generated.gif")
    return gif_path
