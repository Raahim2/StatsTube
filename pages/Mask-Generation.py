import streamlit as st
from PIL import Image
from utils import *

st.title("Mask Generation")

#Generate_Mask


image= st.file_uploader("Choose an image", type=['png', 'jpg', 'jpeg'])








if(image):
    with st.spinner("Summrizing text.."):
        image = Image.open(image)
        result= Generate_Mask(image)
        bg=result[0]['mask']
        st.image(bg)
