import streamlit as st
from PIL import Image , ImageFilter,ImageOps
from utils import *

st.title("Mask Generation")

#Generate_Mask


image= st.file_uploader("Choose an image", type=['png', 'jpg', 'jpeg'])








if(image):
    with st.spinner("Generation mask.."):
        image = Image.open(image)
        result= Generate_Mask(image)

        
        rad = st.slider("Specify blur power", 1, 100)
        bg_mask=result[0]['mask']
        gen= Image.composite(image,Image.new('RGB',image.size,0),bg_mask)
        
        blur = gen.filter(ImageFilter.GaussianBlur(rad))
        
        inverted_mask = ImageOps.invert(bg_mask)
       
        mask_inv_orig = Image.composite(image,Image.new('RGB',image.size,0),inverted_mask)
        
        final = Image.composite(mask_inv_orig,blur,inverted_mask)
        st.image(final)
        
