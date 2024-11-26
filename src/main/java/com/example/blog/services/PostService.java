package com.example.blog.services;


import com.example.blog.repo.PostRepository;
import org.springframework.stereotype.Service;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import com.example.blog.models.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class PostService { 
    private final PostRepository postRepository;
    private final SessionFactory sessionFactory;

    public PostService( PostRepository postRepository, SessionFactory sessionFactory) {
        this.postRepository = postRepository;
        this.sessionFactory = sessionFactory;
    }

    public List<Post> allPosts() {
        return (List<Post>) postRepository.findAll();
    }

    public List<Post> somePosts(int from, int count){
        List<Post> posts = null;
        Session session = null;

        try {
            session = sessionFactory.openSession();

            Query<Post> query = session.createQuery("FROM Post ORDER BY create_at DESC", Post.class);
            query.setFirstResult(from);
            query.setMaxResults(count);

            posts = query.list();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(session != null) session.close();
        }


        return posts;
    }

    public List<Post> somePosts(int from, int count, String filterName){
        filterName = filterName.strip();

        List<Post> posts = null;
        Session session = null;

        try {
            session = sessionFactory.openSession();

            Query<Post> query = session.createQuery("FROM Post p WHERE p.title LIKE :filter OR p.anons LIKE :filter ORDER BY create_at DESC", Post.class);
            query.setParameter("filter",  "%" + filterName + "%");
            query.setFirstResult(from);
            query.setMaxResults(count);

            posts = query.list();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(session != null) session.close();
        }


        return posts;
    }

    public Long postsCount(){
        Long size = 0L;
        Session session = null;

        try {
            session = sessionFactory.openSession();

            Query<Long> query = session.createQuery("SELECT COUNT(p) FROM Post p", Long.class);

            size = query.uniqueResult();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(session != null) session.close();
        }

        return size;
    }

    public Long postsCount(String filter){
        Long size = 0L;
        Session session = null;

        try {
            session = sessionFactory.openSession();

            Query<Long> query = session.createQuery("SELECT COUNT(p) FROM Post p WHERE p.title LIKE :filter OR p.anons LIKE :filter", Long.class);
            query.setParameter("filter", "%" + filter + "%");

            size = query.uniqueResult();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(session != null) session.close();
        }

        return size;
    }

    public Optional<Post> postByID(long id) {
        return postRepository.findById(id);
    }

    public List<Post> postByOwnerId(long ownerId){
        return (List<Post>) postRepository.findByOwnerId(ownerId);
    }

    public List<Post> postByOwnerId(long ownerId, int count){
        List<Post> posts = (List<Post>) postRepository.findByOwnerId(ownerId);
        List<Post> foundPosts = new ArrayList<Post>();
        for (int i = 0; i < posts.size() && foundPosts.size() < count; i++) {
            if (posts.get(i).getOwner().getId() == ownerId) {
                foundPosts.add(posts.get(i));
            }
        }

        return foundPosts;
    }

    public void addPost(Post post) {
        postRepository.save(post);
    }

    public void deletePost(Post post) {
        postRepository.delete(post);
    }
}
