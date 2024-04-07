import streamlit as st
from PIL import Image
from utils import *

st.title("Mask Generation")

#Generate_Mask


image= st.file_uploader("Choose an image", type=['png', 'jpg', 'jpeg'])








if(image):

    with st.spinner("Summrizing text.."):
      st.text("no error")
