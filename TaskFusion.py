import streamlit as st
from utils import *

st.set_page_config(
    page_title="MegaBot",
    page_icon="/Static/chatbot.svg",
)



def card(title , content , link , imlink):
    
    card_html = f"""
    <div class="max-w-sm p-6 bg-white border border-gray-200 rounded-lg shadow dark:bg-gray-800 dark:border-gray-700 my-4 mx-4">
    <img src="{imlink}" class="my-2" alt="robotic" width="40" height="40" />
    <a href="#">
        <h5 class="mb-2 text-2xl font-semibold tracking-tight text-gray-900 dark:text-white">{title}</h5>
    </a>
    <p class="mb-3 font-normal text-gray-500 dark:text-gray-400">{content}</p>
    <a href="{link}" class="inline-flex font-medium items-center text-blue-600 hover:underline">
        View model
        <svg class="w-3 h-3 ms-2.5 rtl:rotate-[270deg]" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 18 18">
            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11v4.833A1.166 1.166 0 0 1 13.833 17H2.167A1.167 1.167 0 0 1 1 15.833V4.167A1.166 1.166 0 0 1 2.167 3h4.618m4.447-2H17v5.768M9.111 8.889l7.778-7.778"/>
        </svg>
    </a>
</div>
    """
    return card_html


st.markdown('<link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">', unsafe_allow_html=True)
st.markdown('<h1 class="text-4xl font-bold text-gray-900 mx-4">Task Fusion.AI</h1>' , unsafe_allow_html=True)
st.markdown('<h1 class="text-xl font-semibold mx-4">Our Models</h1>' , unsafe_allow_html=True)



c1,c2=st.columns(2)
with c1:
    st.markdown(card("ChatBot" , "A smart conversational AI designed to interact with users, providing responses and assistance based on natural language input. It facilitates seamless communication and problem-solving in various domains." ,"ChatBot", "https://cdn.hugeicons.com/icons/robotic-stroke-rounded.svg"), unsafe_allow_html=True)
    st.markdown(card("Image Generation" , "An AI system capable of crafting lifelike images across different domains using advanced algorithms. It generates high-quality visuals, enhancing creativity and aiding in design and content creation tasks." ,"Image-Generation", "https://cdn.hugeicons.com/icons/robotic-stroke-rounded.svg"), unsafe_allow_html=True)
    st.markdown(card("Object Detection" , "A computer vision technology like YOLO (You Only Look Once) rapidly identifies and classifies objects within images or video frames. It enables real-time detection of multiple objects with high accuracy, revolutionizing applications in surveillance, autonomous vehicles, and more.","Object-detector" , "https://cdn.hugeicons.com/icons/robotic-stroke-rounded.svg"), unsafe_allow_html=True)


with c2:
    st.markdown(card("Mask Generation" , "A sophisticated AI system that generates masks or segmentation maps for objects within images. It accurately delineates object boundaries, enabling precise  image editing, applications.","Mask-Generation" , "https://cdn.hugeicons.com/icons/robotic-stroke-rounded.svg"), unsafe_allow_html=True)
    st.markdown(card("Text Classification" , "An AI system that categorizes text into predefined categories or labels based on its content. It aids in organizing and analyzing large volumes of text data, enabling tasks such as sentient analysis and spam filtering.","Text-Classification" , "https://cdn.hugeicons.com/icons/robotic-stroke-rounded.svg"), unsafe_allow_html=True)
    st.markdown(card("Text Summrization" , "Utilizing advanced natural language processing techniques, this AI model condenses lengthy text documents into shorter summaries while retaining the most relevant information. It streamlines information retrieval and aids in quickly understanding the main points of a document." ,"Text-Summrization", "https://cdn.hugeicons.com/icons/robotic-stroke-rounded.svg"), unsafe_allow_html=True)

