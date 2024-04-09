import streamlit as st
from PIL import Image , ImageFilter,ImageOps
from utils import *

st.title("Mask Generation")

#Generate_Mask


image= st.file_uploader("Choose an image", type=['png', 'jpg', 'jpeg'])


options = ["Background", "Hair", "Clothes","Face","Pant"]


selected = st.selectbox("Select what do you want to blur", options)

if(selected=="Background"):
    target=0
if(selected=="Hair"):
    target=1
if(selected=="Clothes"):
    target=2
if(selected=="Face"):
    target=4
if(selected=="Pant"):
    target=5
   


if(image):
    with st.spinner("Generation mask.."):
        image = Image.open(image)
        result= Generate_Mask(image)

        
        rad = st.slider("Specify blur power", 1, 100)
        bg_mask=result[target]['mask']
        gen= Image.composite(image,Image.new('RGB',image.size,0),bg_mask)
        
        blur = gen.filter(ImageFilter.GaussianBlur(rad))
        
        inverted_mask = ImageOps.invert(bg_mask)
       
        mask_inv_orig = Image.composite(image,Image.new('RGB',image.size,0),inverted_mask)
        
        final = Image.composite(mask_inv_orig,blur,inverted_mask)
        st.image(final)
        
