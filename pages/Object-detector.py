import streamlit as st
from utils import Detect

st.header("Object Detctor")

im = st.file_uploader("Uplode a file")

if(im):
    res = Detect(im)
    st.markdown(res)