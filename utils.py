#----------------------------IMPORTING-----------------------------#
import streamlit as st
from transformers import pipeline
from diffusers import DiffusionPipeline
from ctransformers import AutoModelForCausalLM
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

@st.cache_resource
def load_mask_generator():
    path = "Models/Mask-generator"
    if os.path.exists(path):
        pipe = pipeline("image-segmentation", model=path)
    else:
        pipe = pipeline("image-segmentation", model="mattmdjaga/segformer_b2_clothes")
    return pipe

    

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

def Generate_Mask(image):
    model = load_mask_generator()
    result = model(image)
    return result
