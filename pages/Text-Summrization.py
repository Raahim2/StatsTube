import streamlit as st
from utils import Summrize_Text

st.title("Text Summrization")



article = st.text_area("Enter Text of any Artice")


if(article):
    with st.spinner("Summrizing text.."):
        summarized = Summrize_Text(article)
        summarized_text =  summarized[0]['summary_text']
        st.header("Summrized Article")
        st.markdown(summarized_text)