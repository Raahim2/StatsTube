import streamlit as st
from PIL import Image
from utils import Generate_Mask



st.title("Mask Generation")






image= st.file_uploder("Uplode")






if(image):

    with st.spinner("Summrizing text.."):
      st.text("no error")
